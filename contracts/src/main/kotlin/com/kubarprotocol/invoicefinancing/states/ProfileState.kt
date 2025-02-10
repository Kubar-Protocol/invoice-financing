package com.kubarprotocol.invoicefinancing.states

import com.kubarprotocol.invoicefinancing.common.Status
import com.kubarprotocol.invoicefinancing.contracts.ProfileContract
import com.kubarprotocol.invoicefinancing.schema.ProfileSchemaV1
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState
import java.time.Instant

@BelongsToContract(ProfileContract::class)
data class ProfileState(
    val owner: Party,
    val mobileNumber: String,
    val gstUserName: String,
    val gstIn: String,
    val gstInStatus: String,
    val legalBusinessName: String,
    val placeOfBusiness: String,
    val lastModified: Instant = Instant.now(),
    val status: Status,
    override val linearId: UniqueIdentifier = UniqueIdentifier(),
) : LinearState,
    QueryableState {
    // List of participants who have visibility over this state
    override val participants: List<AbstractParty> get() = listOf(owner)

    /**
     * Maps the ProfileState to the corresponding schema object for database storage.
     * Corda uses this when persisting state data to the vault.
     */
    override fun generateMappedObject(schema: MappedSchema): PersistentState =
        when (schema) {
            is ProfileSchemaV1 ->
                ProfileSchemaV1.PersistentProfileV1(
                    this.owner.name.toString(),
                    this.mobileNumber,
                    this.gstUserName,
                    this.gstIn,
                    this.gstInStatus,
                    this.legalBusinessName,
                    this.placeOfBusiness,
                    this.lastModified.toEpochMilli(),
                    this.status.toString(),
                    this.linearId.id,
                )
            else -> throw IllegalArgumentException("Unrecognised schema $schema")
        }

    /**
     * Defines the schema versions supported by this state.
     * When storing new data, the first schema in the list is used by default.
     */
    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(ProfileSchemaV1)
}
