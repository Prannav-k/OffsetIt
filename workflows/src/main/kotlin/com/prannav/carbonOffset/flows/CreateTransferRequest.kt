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


// *********
// * Flows *
// *********
@InitiatingFlow
@StartableByRPC
class CreateTransferRequestInitiator(private val requestTo: Party) : FlowLogic<String>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call(): String {
        val requestFrom = ourIdentity
        val notary = serviceHub.networkMapCache.notaryIdentities.single()

        val output = TransferRequestState(requestFrom, requestTo, "requested")

        val builder = TransactionBuilder(notary)
            .addCommand(TransferRequestContract.Commands.Create(), listOf(requestTo.owningKey, requestFrom.owningKey))
            .addOutputState(output)

        builder.verify(serviceHub)
        val ptx = serviceHub.signInitialTransaction(builder)


        val otherParties: MutableList<Party> = output.participants.stream().map { el: AbstractParty? -> el as Party? }.collect(Collectors.toList())
        otherParties.remove(ourIdentity)
        val sessions = otherParties.stream().map { el: Party? -> initiateFlow(el!!) }.collect(Collectors.toList())

        val stx = subFlow(CollectSignaturesFlow(ptx, sessions))
        subFlow<SignedTransaction>(FinalityFlow(stx, sessions))

        return ("Request for transfer sent successfully with transfer Id " + output.linearId )
    }
}

@InitiatedBy(CreateTransferRequestInitiator::class)
class CreateTransferRequestResponder(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {
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