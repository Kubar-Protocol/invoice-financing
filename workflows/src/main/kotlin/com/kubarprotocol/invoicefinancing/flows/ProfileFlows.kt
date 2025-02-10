package com.kubarprotocol.invoicefinancing.flows

import co.paralleluniverse.fibers.Suspendable
import com.kubarprotocol.invoicefinancing.common.Status
import com.kubarprotocol.invoicefinancing.contracts.ProfileContract
import com.kubarprotocol.invoicefinancing.states.ProfileState
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.node.services.queryBy
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import java.time.Instant

/**
 * Flow to create a new profile on the ledger.
 */
@InitiatingFlow
@StartableByRPC
class CreateProfileFlow(
    private val mobileNumber: String,
    private val gstUserName: String,
    private val gstIn: String,
    private val gstInStatus: String,
    private val legalBusinessName: String,
    private val placeOfBusiness: String,
) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        // 1. Get the identity of the initiating node (owner)
        val owner = ourIdentity

        // 2. Create a new ProfileState instance with provided details
        val profileState =
            ProfileState(
                owner = owner,
                mobileNumber = mobileNumber,
                gstUserName = gstUserName,
                gstIn = gstIn,
                gstInStatus = gstInStatus,
                legalBusinessName = legalBusinessName,
                placeOfBusiness = placeOfBusiness,
                status = Status.ACTIVE,
            )

        // 3. Define the command for profile creation
        val command = Command(ProfileContract.Commands.Create(), owner.owningKey)

        // 4. Retrieve a notary from the network and build the transaction
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val txBuilder =
            TransactionBuilder(notary)
                .addOutputState(profileState, ProfileContract.ID)
                .addCommand(command)

        // 5. Verify and sign the transaction
        txBuilder.verify(serviceHub)
        val signedTx = serviceHub.signInitialTransaction(txBuilder)

        // 6. Finalize the transaction and record it in the ledger
        return subFlow(FinalityFlow(signedTx, emptyList()))
    }
}

/**
 * Flow to update an existing profile on the ledger.
 */
@InitiatingFlow
@StartableByRPC
class UpdateProfileFlow(
    private val linearId: UniqueIdentifier,
    private val newMobileNumber: String,
    private val newGstUserName: String,
    private val newLegalBusinessName: String,
    private val newPlaceOfBusiness: String,
    private val newStatus: Status,
) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        // 1. Retrieve the existing profile state from the vault
        val stateAndRef =
            serviceHub.vaultService
                .queryBy<ProfileState>()
                .states
                .single { it.state.data.linearId == linearId }

        // 2. Verify that the initiator is the owner of the profile
        val owner = ourIdentity
        require(
            stateAndRef.state.data.owner == owner,
        ) { "Only profile owner can update (${owner.name} vs ${stateAndRef.state.data.owner.name})." }

        // 3. Create an updated ProfileState instance with modified details
        val updatedState =
            stateAndRef.state.data.copy(
                mobileNumber = newMobileNumber,
                gstUserName = newGstUserName,
                legalBusinessName = newLegalBusinessName,
                placeOfBusiness = newPlaceOfBusiness,
                status = newStatus,
                lastModified = Instant.now(),
            )

        // 4. Define the command for profile update
        val command = Command(ProfileContract.Commands.Update(), owner.owningKey)

        // 5. Retrieve the notary from the existing state and build the transaction
        val notary = stateAndRef.state.notary
        val txBuilder =
            TransactionBuilder(notary)
                .addInputState(stateAndRef)
                .addOutputState(updatedState, ProfileContract.ID)
                .addCommand(command)

        // 6. Verify and sign the transaction
        txBuilder.verify(serviceHub)
        val signedTx = serviceHub.signInitialTransaction(txBuilder)

        // 7. Finalize the transaction and record it in the ledger
        return subFlow(FinalityFlow(signedTx, emptyList()))
    }
}
