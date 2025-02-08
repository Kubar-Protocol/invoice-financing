package com.kubarprotocol.invoicefinancing.states

import com.kubarprotocol.invoicefinancing.contracts.TemplateContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState
import java.time.Instant
import com.kubarprotocol.invoicefinancing.schema.ProfileSchemaV1

//@BelongsToContract(TemplateContract::class)
data class ProfileState(val owner: Party,
                        val mobileNumber: String,
                        val gstUserName: String,
                        val gstIn: String,
                        val gstInStatus: String,
                        val legalBusinessName: String,
                        val placeOfBusiness: String,
                        val lastModified: Instant = Instant.now(),
                        val status: Status,
                        override val linearId: UniqueIdentifier = UniqueIdentifier()):
    LinearState, QueryableState {
    override val participants: List<AbstractParty> get() = listOf(owner)

    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            is ProfileSchemaV1 -> ProfileSchemaV1.PersistentProfile(
                this.owner.name.toString(),
                this.mobileNumber,
                this.gstUserName,
                this.gstIn,
                this.gstInStatus,
                this.legalBusinessName,
                this.placeOfBusiness,
                this.lastModified.toEpochMilli(),
                this.status.toString(),
                this.linearId.id
            )
            else -> throw IllegalArgumentException("Unrecognised schema $schema")
        }
    }

    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(ProfileSchemaV1)

}

enum class Status {
    ACTIVE, INACTIVE
}
