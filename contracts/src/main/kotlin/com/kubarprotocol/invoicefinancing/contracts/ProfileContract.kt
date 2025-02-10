package com.kubarprotocol.invoicefinancing.contracts

import com.kubarprotocol.invoicefinancing.common.Status
import com.kubarprotocol.invoicefinancing.states.ProfileState
import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction
import java.security.PublicKey

class ProfileContract : Contract {
    companion object {
        const val ID = "1b1f4329-df0d-49d4-9c80-30316fac3ad7"
    }

    interface Commands : CommandData {
        class Create :
            TypeOnlyCommandData(),
            Commands

        class Update :
            TypeOnlyCommandData(),
            Commands
    }

    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<Commands>()
        when (command.value) {
            is Commands.Create -> verifyCreate(tx, command.signers)
            is Commands.Update -> verifyUpdate(tx, command.signers)
        }
    }

    private fun verifyCreate(
        tx: LedgerTransaction,
        signers: List<PublicKey>,
    ) {
        requireThat {
            // Creation rules
            "No inputs should be consumed when creating a new profile." using (tx.inputs.isEmpty())
            "Only one output state should be created." using (tx.outputs.size == 1)

            val output = tx.outputsOfType<ProfileState>().first()

            // Validate fields
            "Owner must be a required signer." using (signers.contains(output.owner.owningKey))
            "Mobile number must be non-empty." using (output.mobileNumber.isNotBlank())
            "GST UserName must be non-empty." using (output.gstUserName.isNotBlank())
            "GST Identification Number must be non-empty." using (output.gstIn.isNotEmpty())
            "GST status must be ACTIVE." using (output.status.toString().equals(Status.ACTIVE.name, true))
            "Legal Business Name must be non-empty." using (output.legalBusinessName.isNotBlank())
            "Place Of Business must be non-empty." using (output.placeOfBusiness.isNotBlank())
            "Status must be ACTIVE." using (output.status.toString().equals(Status.ACTIVE.name, true))
        }
    }

    private fun verifyUpdate(
        tx: LedgerTransaction,
        signers: List<PublicKey>,
    ) {
        requireThat {
            "There must be exactly one input profile." using(tx.inputs.size ==1)
            "There must be exactly one output profile." using(tx.outputs.size ==1)
        }
    }
}
