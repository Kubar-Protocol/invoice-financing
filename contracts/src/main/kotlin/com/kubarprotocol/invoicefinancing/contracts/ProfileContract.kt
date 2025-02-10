package com.kubarprotocol.invoicefinancing.contracts

import com.kubarprotocol.invoicefinancing.common.Status
import com.kubarprotocol.invoicefinancing.states.ProfileState
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.TypeOnlyCommandData
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction
import java.security.PublicKey

/**
 * Contract for validating ProfileState transactions in the Corda ledger.
 */
class ProfileContract : Contract {
    companion object {
        // Unique identifier for this contract
        const val ID = "1b1f4329-df0d-49d4-9c80-30316fac3ad7"
    }

    /**
     * Defines commands supported by this contract: Create and Update.
     */
    interface Commands : CommandData {
        class Create :
            TypeOnlyCommandData(),
            Commands

        class Update :
            TypeOnlyCommandData(),
            Commands
    }

    /**
     * Verifies transactions based on the command type (Create or Update).
     */
    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<Commands>()
        when (command.value) {
            is Commands.Create -> verifyCreate(tx, command.signers)
            is Commands.Update -> verifyUpdate(tx, command.signers)
        }
    }

    /**
     * Verification logic for creating a new profile.
     * Ensures proper input/output structure and valid profile details.
     */
    private fun verifyCreate(
        tx: LedgerTransaction,
        signers: List<PublicKey>,
    ) {
        requireThat {
            // UTXO rules: No inputs, only one output
            "No inputs should be consumed when creating a new profile." using (tx.inputs.isEmpty())
            "Only one output state should be created." using (tx.outputs.size == 1)

            val output = tx.outputsOfType<ProfileState>().first()

            // Ensure the owner has signed the transaction
            "Owner must be a required signer." using (signers.contains(output.owner.owningKey))

            // Field validations
            "Mobile number cannot be empty." using (output.mobileNumber.isNotBlank())
            "GST UserName cannot be empty." using (output.gstUserName.isNotBlank())
            "GST Identification Number cannot be empty." using (output.gstIn.isNotEmpty())
            "GST status must be ACTIVE." using (output.status.toString().equals(Status.ACTIVE.name, true))
            "Legal Business Name cannot be empty." using (output.legalBusinessName.isNotBlank())
            "Place Of Business cannot be empty." using (output.placeOfBusiness.isNotBlank())
            "Status must be ACTIVE." using (output.status.toString().equals(Status.ACTIVE.name, true))
        }
    }

    /**
     * Verification logic for updating an existing profile.
     * Ensures that immutable fields remain unchanged and updates are properly signed.
     */
    private fun verifyUpdate(
        tx: LedgerTransaction,
        signers: List<PublicKey>,
    ) {
        requireThat {
            // UTXO rules: One input and one output
            "There must be exactly one input profile." using (tx.inputs.size == 1)
            "There must be exactly one output profile." using (tx.outputs.size == 1)

            val input = tx.inputsOfType<ProfileState>().single()
            val output = tx.outputsOfType<ProfileState>().single()

            // Ensure immutability of key fields
            "Owner cannot be changed." using (input.owner == output.owner)
            "Linear ID cannot be changed." using (input.linearId == output.linearId)
            "GST Identification Number cannot be changed." using (input.gstIn == output.gstIn)

            // Ensure the owner has signed the transaction
            "Owner must sign the update." using (signers.contains(input.owner.owningKey))

            // Field validations
            "Mobile number cannot be empty." using (output.mobileNumber.isNotBlank())
            "GST UserName cannot be empty." using (output.gstUserName.isNotBlank())
            "GST status cannot be empty." using (output.status.toString().isNotEmpty())
            "Legal Business Name cannot be empty." using (output.legalBusinessName.isNotBlank())
            "Place Of Business cannot be empty." using (output.placeOfBusiness.isNotBlank())
        }
    }
}
