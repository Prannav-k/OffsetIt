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
                "Only one output state should be created when issuing." using (tx.outputs.size == 1)
                "Initial status should be requested only." using (output.status == "requested")
                "Request from and To should not be same." using (output.requestFrom != output.requestTo)
                val signers = command.signers
                "Total signatures should be two" using (signers.size == 2)
            }

            is Commands.Update -> requireThat {
                "Only one input should be consumed when updating." using (tx.inputs.size == 1)
                "Only one output state should be created when updating." using (tx.outputs.size == 1)

                val inputState = tx.inputsOfType<TransferRequestState>().single()
                "Transfer requests can't be accepted if they are already accepted." using (inputState.status == "requested")
                "After accepted status should be accepted only." using (output.status == "accepted")

            }
        }

    }

    // Used to indicate the transaction's intent.
    interface Commands : CommandData {
        class Create : Commands
        class Update : Commands
    }
}