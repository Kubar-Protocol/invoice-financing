package com.kubarprotocol.invoicefinancing.states

import com.kubarprotocol.invoicefinancing.contracts.TemplateContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import java.time.Instant

//@BelongsToContract(TemplateContract::class)
data class ProfileState(
    val profileId: UniqueIdentifier = UniqueIdentifier(),
    val owner: AbstractParty,
    val mobileNumber: String,
    val gstUserName: String,
    val gstIn: String,
    val gstInStatus: String,
    val legalBusinessName: String,
    val placeOfBusiness: String,
    val lastModified: Instant = Instant.now(),
    val status: Status,
    override val participants: List<AbstractParty> = listOf(owner),
): ContractState

enum class Status {
    ACTIVE, INACTIVE
}
