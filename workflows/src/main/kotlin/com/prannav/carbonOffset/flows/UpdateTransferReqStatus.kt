package com.prannav.carbonOffset.flows

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.*
import net.corda.core.utilities.ProgressTracker
import net.corda.core.flows.FinalityFlow

import net.corda.core.flows.CollectSignaturesFlow

import net.corda.core.transactions.SignedTransaction

import java.util.stream.Collectors

import net.corda.core.flows.FlowSession

import net.corda.core.identity.Party


import net.corda.core.transactions.TransactionBuilder

import net.corda.core.contracts.requireThat
import net.corda.core.identity.AbstractParty
import com.prannav.carbonOffset.contracts.TransferRequestContract
import com.prannav.carbonOffset.states.TransferRequestState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria


// *********
// * Flows *
// *********
@InitiatingFlow
@StartableByRPC
class UpdateTransferReqStatusInitiator(private val reqId: String) : FlowLogic<String>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call(): String {

        val transferReqInputCriteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(UniqueIdentifier.fromString(reqId)))
        val transferReqStateAndRef = serviceHub.vaultService.queryBy<TransferRequestState>(criteria = transferReqInputCriteria).states.single()
        val inputTransferReqState = transferReqStateAndRef.state.data

        val notary = serviceHub.networkMapCache.notaryIdentities.single()

        val output = inputTransferReqState.withNewStatus("accepted")

        val builder = TransactionBuilder(notary)
            .addCommand(TransferRequestContract.Commands.Update(), listOf(inputTransferReqState.requestTo.owningKey, inputTransferReqState.requestFrom.owningKey))
            .addOutputState(output)
            .addInputState(transferReqStateAndRef)

        logger.info("***** , ost is $output")

        builder.verify(serviceHub)
        val ptx = serviceHub.signInitialTransaction(builder)

        logger.info("Txn signed locally")
        val otherParties: MutableList<Party> = output.participants.stream().map { el: AbstractParty? -> el as Party? }.collect(Collectors.toList())
        otherParties.remove(ourIdentity)
        val sessions = otherParties.stream().map { el: Party? -> initiateFlow(el!!) }.collect(Collectors.toList())

        val stx = subFlow(CollectSignaturesFlow(ptx, sessions))
        logger.info("Transfer status accepted")
        subFlow<SignedTransaction>(FinalityFlow(stx, sessions))

        return ("Transfer req updated " + output.linearId )
    }
}

@InitiatedBy(UpdateTransferReqStatusInitiator::class)
class UpdateTransferReqStatusResponder(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        val signTransactionFlow = object : SignTransactionFlow(counterpartySession) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
            }
        }
        val txId = subFlow(signTransactionFlow).id
        return subFlow(ReceiveFinalityFlow(counterpartySession, expectedTxId = txId))
    }
}