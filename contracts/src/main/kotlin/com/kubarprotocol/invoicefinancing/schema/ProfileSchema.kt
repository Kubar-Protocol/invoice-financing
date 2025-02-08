package com.kubarprotocol.invoicefinancing.schema

import com.kubarprotocol.invoicefinancing.states.Status
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import org.hibernate.annotations.Type
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table


object ProfileSchema

object ProfileSchemaV1 : MappedSchema(
    schemaFamily = ProfileSchema.javaClass,
    version = 1,
    mappedTypes = listOf(PersistentProfile::class.java)) {

    @Entity
    @Table(name = "profile_state")
    class PersistentProfile(
        @Column(name = "owner")
        val owner: String,

        @Column(name ="mobile_number")
        val mobileNumber: String,

        @Column(name = "gst_user_name")
        val gstUserName: String,

        @Column(name = "gst_in")
        val gstIn: String,

        @Column(name = "gst_in_status")
        val gstInStatus: String,

        @Column(name = "legal_business_name")
        val legalBusinessName: String,

        @Column(name = "place_of_business")
        val placeOfBusiness: String,

        @Column(name = "last_modified")
        val lastModified: Long,

        @Column(name = "status")
        var status: String,

        @Column(name = "linear_id")
        @Type(type = "uuid-char")
        var linearId: UUID

        ) : PersistentState () {
            constructor(): this (
                owner = "",
                mobileNumber = "",
                gstUserName = "",
                gstIn = "",
                gstInStatus = "",
                legalBusinessName = "",
                placeOfBusiness = "",
                lastModified =  0L,
                status = Status.ACTIVE.name,
                linearId = UUID.randomUUID()
            )
        }

}