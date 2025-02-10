package com.kubarprotocol.invoicefinancing.states

import com.kubarprotocol.invoicefinancing.common.Status
import net.corda.core.identity.CordaX500Name
import net.corda.testing.core.TestIdentity
import org.junit.Test
import kotlin.test.assertEquals

class ProfileStateTest {
    private val owner = TestIdentity(CordaX500Name("Alice", "London", "GB")).party

    @Test
    fun `ProfileState should have correct fields`() {
        val profileState =
            ProfileState(
                owner = owner,
                mobileNumber = "1234567890",
                gstUserName = "AliceGST",
                gstIn = "1234567890ABCDEFGHI",
                gstInStatus = "Active",
                legalBusinessName = "Alice Corporation Ltd",
                placeOfBusiness = "India",
                status = Status.ACTIVE,
            )

        assertEquals(owner, profileState.owner)
        assertEquals("1234567890", profileState.mobileNumber)
        assertEquals("AliceGST", profileState.gstUserName)
        assertEquals("1234567890ABCDEFGHI", profileState.gstIn)
        assertEquals("Active", profileState.gstInStatus)
        assertEquals("Alice Corporation Ltd", profileState.legalBusinessName)
        assertEquals("India", profileState.placeOfBusiness)
        assertEquals(Status.ACTIVE, profileState.status)
    }

    @Test
    fun `Participants should only include owner`() {
        val profileState =
            ProfileState(
                owner = owner,
                mobileNumber = "1234567890",
                gstUserName = "AliceGST",
                gstIn = "1234567890ABCDEFGHI",
                gstInStatus = "Active",
                legalBusinessName = "Alice Corporation Ltd",
                placeOfBusiness = "India",
                status = Status.ACTIVE,
            )

        assertEquals(1, profileState.participants.size)
        assertEquals(owner, profileState.participants[0])
    }

    @Test
    fun `ProfileState should be able to change to Inactive`() {
        val profileState =
            ProfileState(
                owner = owner,
                mobileNumber = "1234567890",
                gstUserName = "AliceGST",
                gstIn = "1234567890ABCDEFGHI",
                gstInStatus = "Active",
                legalBusinessName = "Alice Corporatiion Ltd",
                placeOfBusiness = "India",
                status = Status.ACTIVE,
            )

        val updatedProfileState = profileState.copy(status = Status.INACTIVE)

        assertEquals(Status.ACTIVE, profileState.status)
        assertEquals(Status.INACTIVE, updatedProfileState.status)
    }
}
