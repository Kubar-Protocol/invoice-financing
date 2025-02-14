/*
 * Copyright 2025 Kubar Protocol
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kubarprotocol.invoicefinancing.schema

import com.kubarprotocol.invoicefinancing.common.Status
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import org.hibernate.annotations.Type
import java.util.UUID
import javax.persistence.*

/**
 * Schema family for Profile states.
 * Used to group schema versions related to profile data persistence in Corda.
 */
object ProfileSchema

/**
 * First version of the Profile state schema.
 * Defines the database mapping structure for profile states at version 1.
 *
 * @property mappedTypes List of JPA entity classes to be mapped to the database
 */
object ProfileSchemaV1 : MappedSchema(
    schemaFamily = ProfileSchema.javaClass,
    version = 1,
    mappedTypes = listOf(PersistentProfileV1::class.java),
) {
    /**
     * JPA entity representation of a Profile state.
     * Maps all fields required to persist profile states in the Corda database.
     *
     * @property owner Corda party name (X500) owning this profile
     * @property mobileNumber User's mobile contact number
     * @property gstUserName Name associated with GST registration
     * @property gstIn GST Identification Number
     * @property gstInStatus Current status of GSTIN verification
     * @property legalBusinessName Officially registered business name
     * @property placeOfBusiness Primary business location/address
     * @property lastModified Timestamp of last profile modification
     * @property status Current lifecycle status of the profile (ACTIVE/INACTIVE)
     * @property linearId Unique identifier used for state evolution tracking
     */
    @Entity
    @Table(name = "profile_state",
        indexes = [Index( name="profile_state_owner_idx", columnList = "owner"),
        Index( name="profile_state_linear_id_idx", columnList = "linear_id"), ])
    class PersistentProfileV1(
        @Column(name = "owner")
        val owner: String,
        @Column(name = "mobile_number")
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
        @Enumerated(EnumType.STRING)
        @Column(name = "status", nullable = false, length = 30)
        var status: Status,
        @Column(name = "linear_id")
        @Type(type = "uuid-char")
        var linearId: UUID,
    ) : PersistentState()
}
