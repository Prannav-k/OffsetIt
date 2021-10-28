package com.prannav.carbonOffset.flows

import co.paralleluniverse.fibers.Suspendable
import com.prannav.carbonOffset.states.OffsetTokenState
import com.r3.corda.lib.tokens.contracts.types.TokenPointer
import com.r3.corda.lib.tokens.workflows.flows.rpc.RedeemNonFungibleTokens
import com.r3.corda.lib.tokens.workflows.flows.rpc.RedeemNonFungibleTokensHandler
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.utilities.ProgressTracker

// *********
// * Flows *
// *********
@InitiatingFlow
@StartableByRPC
class RedeemOffsetFlow(val offsetId: String) : FlowLogic<String>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call():String {
        //get token state details
        val inputCriteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(UniqueIdentifier.fromString(offsetId)))
        val offsetStateAndRef = serviceHub.vaultService.queryBy<OffsetTokenState>(criteria = inputCriteria).states.single()
        val offsetTokenState = offsetStateAndRef.state.data

        //Fetch token issuer
        val issuer = offsetTokenState.issuer

        //Fetch the pointer
        val offsetTokenStatePointer: TokenPointer<*> = offsetTokenState.toPointer(offsetTokenState.javaClass)

        val stx = subFlow(RedeemNonFungibleTokens(offsetTokenStatePointer, issuer))
        return "Offset NFT redeemed " + stx.id
    }
}

@InitiatedBy(RedeemOffsetFlow::class)
class RedeemOffsetFlowResponder(val counterpartySession: FlowSession) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        // Responder flow logic goes here.
        subFlow(RedeemNonFungibleTokensHandler(counterpartySession));
    }
}