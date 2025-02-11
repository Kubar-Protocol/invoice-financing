package com.kubarprotocol.invoicefinancing.contracts

import com.kubarprotocol.invoicefinancing.common.Status
import com.kubarprotocol.invoicefinancing.states.ProfileState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.CordaX500Name
import net.corda.testing.core.TestIdentity
import net.corda.testing.node.MockServices
import net.corda.testing.node.ledger
import org.junit.Test

class ProfileContractTest {
    private val ledgerServices: MockServices = MockServices(listOf("com.kubarprotocol.invoicefinancing.contracts"))

    private val alice = TestIdentity(CordaX500Name("Alice", "London", "GB"))
    private val bob = TestIdentity(CordaX500Name("Bob", "New York", "US"))
    private val validProfileState =
        ProfileState(
            mobileNumber = "0987654321",
            gstUserName = "GSTUser",
            gstIn = "GST123456789",
            gstInStatus = "ACTIVE",
            legalBusinessName = "Test Business",
            placeOfBusiness = "Test Place of Business",
            status = Status.ACTIVE,
            owner = alice.party,
            linearId = UniqueIdentifier(),
        )

    @Test
    fun `create transaction with valid parameters should verify`() {
        ledgerServices.ledger {
            transaction {
                output(ProfileContract.ID, validProfileState)
                command(alice.publicKey, ProfileContract.Commands.Create())
                verifies()
            }
        }
    }

    @Test
    fun `create transaction with empty mobile number should fail`() {
        val invalidState = validProfileState.copy(mobileNumber = "")
        ledgerServices.ledger {
            transaction {
                output(ProfileContract.ID, invalidState)
                command(alice.publicKey, ProfileContract.Commands.Create())
                failsWith("Mobile number cannot be empty.")
            }
        }
    }

    @Test
    fun `create transaction with empty GST UserName should fail`() {
        val invalidState = validProfileState.copy(gstUserName = "")
        ledgerServices.ledger {
            transaction {
                output(ProfileContract.ID, invalidState)
                command(alice.publicKey, ProfileContract.Commands.Create())
                failsWith("GST UserName cannot be empty.")
            }
        }
    }

    @Test
    fun `create transaction with empty GST Identification Number should fail`() {
        val invalidState = validProfileState.copy(gstIn = "")
        ledgerServices.ledger {
            transaction {
                output(ProfileContract.ID, invalidState)
                command(alice.publicKey, ProfileContract.Commands.Create())
                failsWith("GST Identification Number cannot be empty.")
            }
        }
    }

    @Test
    fun `create transaction with invalid legal business name should fail`() {
        val invalidState = validProfileState.copy(legalBusinessName = "")
        ledgerServices.ledger {
            transaction {
                output(ProfileContract.ID, invalidState)
                command(alice.publicKey, ProfileContract.Commands.Create())
                failsWith("Legal Business Name cannot be empty.")
            }
        }
    }

    @Test
    fun `create transaction with invalid place of business should fail`() {
        val invalidState = validProfileState.copy(placeOfBusiness = "")
        ledgerServices.ledger {
            transaction {
                output(ProfileContract.ID, invalidState)
                command(alice.publicKey, ProfileContract.Commands.Create())
                failsWith("Place Of Business cannot be empty.")
            }
        }
    }

    @Test
    fun `create transaction with invalid status should fail`() {
        val invalidState = validProfileState.copy(status = Status.INACTIVE)
        ledgerServices.ledger {
            transaction {
                output(ProfileContract.ID, invalidState)
                command(alice.publicKey, ProfileContract.Commands.Create())
                failsWith("Status must be ACTIVE.")
            }
        }
    }

    @Test
    fun `create transaction with inputs should fail`() {
        ledgerServices.ledger {
            transaction {
                input(ProfileContract.ID, validProfileState)
                output(ProfileContract.ID, validProfileState)
                command(alice.publicKey, ProfileContract.Commands.Create())
                failsWith("No inputs should be consumed when creating a new profile.")
            }
        }
    }

    @Test
    fun `create transaction with multiple output should fail`() {
        ledgerServices.ledger {
            transaction {
                output(ProfileContract.ID, validProfileState)
                output(ProfileContract.ID, validProfileState) // Adding a second output
                command(alice.publicKey, ProfileContract.Commands.Create())
                failsWith("Only one output state should be created.")
            }
        }
    }

    @Test
    fun `create transaction without owner signature should fail`() {
        ledgerServices.ledger {
            transaction {
                output(ProfileContract.ID, validProfileState)
                command(bob.publicKey, ProfileContract.Commands.Create())
                failsWith("Owner must be a required signer.")
            }
        }
    }

    @Test
    fun `update transaction with multiple inputs should fail`() {
        val validProfileState2 = validProfileState.copy(mobileNumber = "888888888")
        val updatedState = validProfileState.copy(mobileNumber = "999999999")
        ledgerServices.ledger {
            transaction {
                input(ProfileContract.ID, validProfileState)
                input(ProfileContract.ID, validProfileState2) // Adding a second input
                output(ProfileContract.ID, updatedState)
                command(alice.publicKey, ProfileContract.Commands.Update())
                failsWith("There must be exactly one input profile.")
            }
        }
    }

    @Test
    fun `update transaction with multiple outputs should fail`() {
        val updatedState = validProfileState.copy(mobileNumber = "999999999")
        ledgerServices.ledger {
            transaction {
                input(ProfileContract.ID, validProfileState)
                output(ProfileContract.ID, updatedState)
                output(ProfileContract.ID, updatedState) // Adding a second output
                command(alice.publicKey, ProfileContract.Commands.Update())
                failsWith("There must be exactly one output profile.")
            }
        }
    }

    @Test
    fun `update transaction changing owner should fail`() {
        val updatedState = validProfileState.copy(owner = bob.party)
        ledgerServices.ledger {
            transaction {
                input(ProfileContract.ID, validProfileState)
                output(ProfileContract.ID, updatedState)
                command(alice.publicKey, ProfileContract.Commands.Update())
                failsWith("Owner cannot be changed.")
            }
        }
    }

    @Test
    fun `update transaction changing linearId should fail`() {
        val updatedState = validProfileState.copy(linearId = UniqueIdentifier())
        ledgerServices.ledger {
            transaction {
                input(ProfileContract.ID, validProfileState)
                output(ProfileContract.ID, updatedState)
                command(alice.publicKey, ProfileContract.Commands.Update())
                failsWith("Linear ID cannot be changed.")
            }
        }
    }

    @Test
    fun `update transaction changing GST Identification Number should fail`() {
        val updatedState = validProfileState.copy(gstIn = "new GST Number")
        ledgerServices.ledger {
            transaction {
                input(ProfileContract.ID, validProfileState)
                output(ProfileContract.ID, updatedState)
                command(alice.publicKey, ProfileContract.Commands.Update())
                failsWith("GST Identification Number cannot be changed.")
            }
        }
    }

    @Test
    fun `update transaction owner does not sign the update should fail`() {
        val updatedState = validProfileState.copy(mobileNumber = "999999999")
        ledgerServices.ledger {
            transaction {
                input(ProfileContract.ID, validProfileState)
                output(ProfileContract.ID, updatedState)
                command(bob.publicKey, ProfileContract.Commands.Update()) // bob signed instead of alice
                failsWith("Owner must sign the update.")
            }
        }
    }

    @Test
    fun `update transaction with valid parameters should verify`() {
        val updatedState = validProfileState.copy(mobileNumber = "999888111")
        ledgerServices.ledger {
            transaction {
                input(ProfileContract.ID, validProfileState)
                output(ProfileContract.ID, updatedState)
                command(alice.publicKey, ProfileContract.Commands.Update())
                verifies()
            }
        }
    }

    @Test
    fun `update transaction with invalid Mobile Number should fail`() {
        val updatedState = validProfileState.copy(mobileNumber = "")
        ledgerServices.ledger {
            transaction {
                input(ProfileContract.ID, validProfileState)
                output(ProfileContract.ID, updatedState)
                command(alice.publicKey, ProfileContract.Commands.Update())
                failsWith("Mobile number cannot be empty.")
            }
        }
    }

    @Test
    fun `update transaction with invalid GST UserName should fail`() {
        val updatedState = validProfileState.copy(gstUserName = "")
        ledgerServices.ledger {
            transaction {
                input(ProfileContract.ID, validProfileState)
                output(ProfileContract.ID, updatedState)
                command(alice.publicKey, ProfileContract.Commands.Update())
                failsWith("GST UserName cannot be empty.")
            }
        }
    }

    @Test
    fun `update transaction with invalid Legal Business Name should fail`() {
        val updatedState = validProfileState.copy(legalBusinessName = "")
        ledgerServices.ledger {
            transaction {
                input(ProfileContract.ID, validProfileState)
                output(ProfileContract.ID, updatedState)
                command(alice.publicKey, ProfileContract.Commands.Update())
                failsWith("Legal Business Name cannot be empty.")
            }
        }
    }

    @Test
    fun `update transaction with invalid Place Of Business should fail`() {
        val updatedState = validProfileState.copy(placeOfBusiness = "")
        ledgerServices.ledger {
            transaction {
                input(ProfileContract.ID, validProfileState)
                output(ProfileContract.ID, updatedState)
                command(alice.publicKey, ProfileContract.Commands.Update())
                failsWith("Place Of Business cannot be empty.")
            }
        }
    }

    @Test
    fun `update transaction with invalid GST Status should fail`() {
        val updatedState = validProfileState.copy(gstInStatus = "")
        ledgerServices.ledger {
            transaction {
                input(ProfileContract.ID, validProfileState)
                output(ProfileContract.ID, updatedState)
                command(alice.publicKey, ProfileContract.Commands.Update())
                failsWith("GST status cannot be empty.")
            }
        }
    }
}
