package com.prannav.carbonOffset.flows

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.tokens.contracts.states.NonFungibleToken
import com.r3.corda.lib.tokens.contracts.utilities.issuedBy
import com.r3.corda.lib.tokens.contracts.utilities.withNotary
import com.r3.corda.lib.tokens.workflows.flows.rpc.CreateEvolvableTokens
import com.r3.corda.lib.tokens.workflows.flows.rpc.IssueTokens
import net.corda.core.contracts.Amount
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import net.corda.core.utilities.ProgressTracker
import com.prannav.carbonOffset.states.OffsetTokenState
import java.util.*

// *********
// * Flows *
// *********
@StartableByRPC
class CreateAndIssueOffsetToken(val owner: Party,
                                val offsetPrice: Amount<Currency>,
                                val offsetType: String,
                                val offsetUnit: String,
                                val source: String,
                                val otherInfo : String,
                                val expiryDays : Int
                               ) : FlowLogic<String>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call():String {

        val notary = serviceHub.networkMapCache.notaryIdentities.single() // METHOD 1

        //Create output state , fetching node details from ourIdentity
        val offsetState = OffsetTokenState(UniqueIdentifier(),Arrays.asList(ourIdentity),offsetPrice,offsetType,offsetUnit,source,otherInfo,expiryDays)
        val transactionState = offsetState withNotary notary

        //Create token
        subFlow(CreateEvolvableTokens(transactionState))

        //Create token from above reference state
        val issuedOffsetToken = offsetState.toPointer(offsetState.javaClass) issuedBy  ourIdentity
        val offsetToken = NonFungibleToken(issuedOffsetToken, owner, UniqueIdentifier())

        //Issue the offset
        val stx = subFlow(IssueTokens(listOf(offsetToken)))

        return ("The offset is now a nft with id " + offsetState.linearId + " , Transaction ID: " + stx.id)
    }
}
