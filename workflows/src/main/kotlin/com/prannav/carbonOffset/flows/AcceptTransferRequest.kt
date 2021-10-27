package com.prannav.carbonOffset.flows

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.tokens.contracts.states.FungibleToken
import com.r3.corda.lib.tokens.money.FiatCurrency.Companion.getInstance
import com.r3.corda.lib.tokens.selection.database.selector.DatabaseTokenSelection
import com.r3.corda.lib.tokens.workflows.flows.move.addMoveNonFungibleTokens
import com.r3.corda.lib.tokens.workflows.flows.move.addMoveTokens
import com.r3.corda.lib.tokens.workflows.internal.flows.distribution.UpdateDistributionListFlow
import net.corda.core.contracts.Amount
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.unwrap
import com.prannav.carbonOffset.states.OffsetTokenState
import com.prannav.carbonOffset.states.TransferRequestState
import java.util.*

// *********
// * Flows *
// *********
@InitiatingFlow
@StartableByRPC
class AcceptTransferRequest(
                 val transferReqId : String,
                 val offsetId: String) : FlowLogic<String>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call():String {

        val notary = serviceHub.networkMapCache.notaryIdentities.single()

        //get the transfer req
        val transferReqInputCriteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(UniqueIdentifier.fromString(transferReqId)))
        val transferReqStateAndRef = serviceHub.vaultService.queryBy<TransferRequestState>(criteria = transferReqInputCriteria).states.single()
        val transferReqState = transferReqStateAndRef.state.data
        val buyer = transferReqState.requestFrom

        logger.info("Fetched transfer req state ${transferReqState}")
        // Get the offset state from provided id
        val inputCriteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(UniqueIdentifier.fromString(offsetId)))
        val offsetStateAndRef = serviceHub.vaultService.queryBy<OffsetTokenState>(criteria = inputCriteria).states.single()
        val offsetTokenState = offsetStateAndRef.state.data
        logger.info("offset token fetched is $offsetTokenState");
        //Build the txn
        val txBuilder = TransactionBuilder(notary)


        //Receive amount and transfer token
        addMoveNonFungibleTokens(txBuilder, serviceHub, offsetTokenState.toPointer(offsetTokenState.javaClass), buyer)
        val buyerSession = initiateFlow(buyer)
        buyerSession.send(offsetTokenState.offsetPrice)
        val inputs = subFlow(ReceiveStateAndRefFlow<FungibleToken>(buyerSession))
        val moneyReceived: List<FungibleToken> = buyerSession.receive<List<FungibleToken>>().unwrap { it -> it}
        addMoveTokens(txBuilder, inputs, moneyReceived)
        val initialSignedTxn = serviceHub.signInitialTransaction(txBuilder)

        val ftx= subFlow(CollectSignaturesFlow(initialSignedTxn, listOf(buyerSession)))
        val stx = subFlow(FinalityFlow(ftx, listOf(buyerSession)))
        subFlow(UpdateDistributionListFlow(stx))
        return ("\nOffset transferred to " + buyer.name.organisation + "\nTransaction ID: " + stx.id)
    }
}

@InitiatedBy(AcceptTransferRequest::class)
class SellOffsetResponder(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call():SignedTransaction {
        // Get values of offset
        val price = counterpartySession.receive<Amount<Currency>>().unwrap { it }
        val priceToken = Amount(price.quantity, getInstance(price.token.currencyCode))

        // Transfer the amount
        val inputsAndOutputs : Pair<List<StateAndRef<FungibleToken>>, List<FungibleToken>> =
                DatabaseTokenSelection(serviceHub).generateMove(listOf(Pair(counterpartySession.counterparty,priceToken)),ourIdentity)
        subFlow(SendStateAndRefFlow(counterpartySession, inputsAndOutputs.first))
        counterpartySession.send(inputsAndOutputs.second)

        subFlow(object : SignTransactionFlow(counterpartySession) {
            @Throws(FlowException::class)
            override fun checkTransaction(stx: SignedTransaction) {
            }
        })
        return subFlow(ReceiveFinalityFlow(counterpartySession))
    }
}
