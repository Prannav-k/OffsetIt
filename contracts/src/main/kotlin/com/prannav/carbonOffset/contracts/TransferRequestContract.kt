package com.prannav.carbonOffset.contracts

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.transactions.LedgerTransaction
import net.corda.core.contracts.requireThat
import com.prannav.carbonOffset.states.TransferRequestState

// ************
// * Contract *
// ************
class TransferRequestContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        const val ID = "com.prannav.carbonOffset.contracts.TransferRequestContract"
    }

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    override fun verify(tx: LedgerTransaction) {
        // Verification logic goes here.
        val command = tx.commands.requireSingleCommand<Commands>()
        val output = tx.outputsOfType<TransferRequestState>().first()
        when (command.value) {
            is Commands.Create -> requireThat {
                "No inputs should be consumed when sending the transfer req.".using(tx.inputStates.isEmpty())
            }

            is Commands.Update -> requireThat {
            }
        }

    }

    // Used to indicate the transaction's intent.
    interface Commands : CommandData {
        class Create : Commands
        class Update : Commands
    }
}