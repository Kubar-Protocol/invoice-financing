package com.kubarprotocol.invoicefinancing

import com.kubarprotocol.invoicefinancing.contracts.ProfileContract
import com.kubarprotocol.invoicefinancing.flows.CreateProfileFlow
import com.kubarprotocol.invoicefinancing.states.ProfileState
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import net.corda.testing.common.internal.testNetworkParameters
import net.corda.testing.node.*
import org.junit.After
import org.junit.Before
import org.junit.Test

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
        val future = alice.startFlow(flow)
        mockNetwork.runNetwork()
        val signedTx =future.get()

        // Verify transaction structure
        with(signedTx.tx) {
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
    /*
    class CreateProfileFlow(
    private val mobileNumber: String,
    private val gstUserName: String,
    private val gstIn: String,
    private val gstInStatus: String,
    private val legalBusinessName: String,
    private val placeOfBusiness: String,
) : FlowLogic<SignedTransaction>() {
     */
}