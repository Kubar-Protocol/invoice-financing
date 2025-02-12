package com.kubarprotocol.invoicefinancing

import com.kubarprotocol.invoicefinancing.common.Status
import com.kubarprotocol.invoicefinancing.contracts.ProfileContract
import com.kubarprotocol.invoicefinancing.flows.CreateProfileFlow
import com.kubarprotocol.invoicefinancing.flows.UpdateProfileFlow
import com.kubarprotocol.invoicefinancing.states.ProfileState
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import net.corda.core.contracts.UniqueIdentifier
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import net.corda.testing.common.internal.testNetworkParameters
import net.corda.testing.node.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.concurrent.ExecutionException

class ProfileFlowsTest {
    private lateinit var mockNetwork: MockNetwork
    private lateinit var alice: StartedMockNode
    private lateinit var bob: StartedMockNode
    private lateinit var notary: StartedMockNode

    @Before
    fun setup() {
        mockNetwork = MockNetwork(
            MockNetworkParameters(
                networkParameters = testNetworkParameters(minimumPlatformVersion = 4),
                cordappsForAllNodes = listOf(
                    TestCordapp.findCordapp("com.kubarprotocol.invoicefinancing.contracts"),
                    TestCordapp.findCordapp("com.kubarprotocol.invoicefinancing.flows")
                ),
            )
        )
        notary=mockNetwork.defaultNotaryNode
        alice=mockNetwork.createPartyNode()
        bob=mockNetwork.createPartyNode()

    }

    @After
    fun tearDown() {
        mockNetwork.stopNodes()
    }

    @Test
    fun `create profile flow should produce valid transaction`() {
        val flow = CreateProfileFlow(
            mobileNumber = "1234567890",
            gstUserName = "alice_gst",
            gstIn = "GSTIN0000001",
            gstInStatus = "ACTIVE",
            legalBusinessName = "Test Business Name Ltd",
            placeOfBusiness = "Test Place Of Business Name Ltd"
        )

        val signedTx= alice.startFlow(flow).also { mockNetwork.runNetwork() }.get()

        // Verify transaction structure
        with(signedTx.tx) {
            // Check UTXO inputs/outputs
            assertEquals(0, inputs.size)
            assertEquals(1, outputs.size)

            // Check outputs
            val output = outputs.single().data as ProfileState
            assertEquals(output.mobileNumber, "1234567890")
            assertEquals(output.gstUserName, "alice_gst")
            assertEquals(output.gstIn, "GSTIN0000001")
            assertEquals(output.gstInStatus, "ACTIVE")
            assertEquals(output.legalBusinessName, "Test Business Name Ltd")
            assertEquals(output.placeOfBusiness, "Test Place Of Business Name Ltd")

            // Check commands
            val command = commands.single()
            assertTrue(command.value is ProfileContract.Commands.Create)
            assertEquals(alice.info.legalIdentities.single().owningKey, command.signers.single())
        }

    }

    @Test
    fun `update profile flow should modify existing state1`() {
        // First create a profile
        val createFlow = CreateProfileFlow(
            mobileNumber = "1234567890",
            gstUserName = "alice_gst",
            gstIn = "GSTIN0000001",
            gstInStatus = "ACTIVE",
            legalBusinessName = "Test Business Name Ltd",
            placeOfBusiness = "Test Place Of Business Name Ltd"
        )
        val createTx=alice.startFlow(createFlow).also { mockNetwork.runNetwork() }.get()
        val createdState=createTx.tx.outputsOfType<ProfileState>().single()

        // Run update flow
        val updateFlow = UpdateProfileFlow(
            linearId= createdState.linearId,
            newMobileNumber = "+6512345678",
            newGstUserName = "alice_gst",
            newLegalBusinessName = "Test Business Name Pte Ltd",
            newPlaceOfBusiness = "Singapore Central",
            newStatus = Status.ACTIVE,
        )
        val signedTx= alice.startFlow(updateFlow).also { mockNetwork.runNetwork() }.get()

        with(signedTx.tx) {
            // Check UTXO inputs/outputs
            assertEquals(1, inputs.size)
            assertEquals(1, outputs.size)

            // Check outputs
            val input = inputs.single().let { stateRef -> alice.services.toStateAndRef<ProfileState>(stateRef).state.data  }
            val output = outputs.single().data as ProfileState

            assertEquals("+6512345678", output.mobileNumber)
            assertEquals("alice_gst", output.gstUserName)
            assertEquals("Test Business Name Pte Ltd", output.legalBusinessName)
            assertEquals("Singapore Central", output.placeOfBusiness)
            assertEquals(Status.ACTIVE,output.status)

            // Check immutables
            assertEquals(input.linearId, output.linearId)
            assertEquals(input.owner, output.owner)
            assertEquals(input.gstIn, output.gstIn)

            // Check commands
            val command = commands.single()
            assertTrue(command.value is ProfileContract.Commands.Update)
            assertEquals(alice.info.legalIdentities.single().owningKey, command.signers.single())

        }

    }

    @Test
    fun `non-owner should not be able to update profile`() {
        // First create a profile with Alice
        val createTx = alice.startFlow( CreateProfileFlow(
            mobileNumber = "1234567890",
            gstUserName = "alice_gst",
            gstIn = "GSTIN0000001",
            gstInStatus = "ACTIVE",
            legalBusinessName = "Test Business Name Ltd",
            placeOfBusiness = "Test Place Of Business Name Ltd",
        )).also { mockNetwork.runNetwork() }.get()

        val createdState=createTx.tx.outputsOfType<ProfileState>().single()


        // Bob tries to unauthorized update
        val updateFlow = UpdateProfileFlow(
            linearId= createdState.linearId,
            newMobileNumber = "+6512345678",
            newGstUserName = "alice_gst",
            newLegalBusinessName = "Test Business Name Pte Ltd",
            newPlaceOfBusiness = "Singapore Central",
            newStatus = Status.ACTIVE,
        )

        val ex = assertFailsWith<ExecutionException> {
            bob.startFlow(updateFlow).also { mockNetwork.runNetwork() }.get()
        }

        assertTrue( ex.cause is NoSuchElementException, "Expected cause to be NoSuchElementException")

    }

    @Test
    fun `update flow should fail for non-existent profile`() {
        val nonExistentId=  UniqueIdentifier()

        // alice tries to update non existence profile
        val updateFlow = UpdateProfileFlow(
            linearId = nonExistentId,
            newMobileNumber = "+987654321",
            newGstUserName = "updated_gst",
            newLegalBusinessName = "New Corp",
            newPlaceOfBusiness = "Paris",
            newStatus = Status.INACTIVE
        )

        val ex = assertFailsWith<ExecutionException> {
            alice.startFlow(updateFlow).also { mockNetwork.runNetwork() }.get()
        }

       assertTrue( ex.cause is NoSuchElementException, "Expected cause to be NoSuchElementException")
    }

}