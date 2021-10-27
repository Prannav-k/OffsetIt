package com.prannav.carbonOffset.contracts

import com.r3.corda.lib.tokens.contracts.EvolvableTokenContract
import net.corda.core.contracts.Contract
import net.corda.core.contracts.Requirements
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction
import com.prannav.carbonOffset.states.HouseState

// ************
// * Contract *
// ************
class HouseContract : EvolvableTokenContract(),Contract {
    companion object {
        const val CONTRACT_ID = "com.prannav.carbonOffset.contracts.HouseContract"
    }
    override fun additionalCreateChecks(tx: LedgerTransaction) {
        // Write contract validation logic to be performed while creation of token
        val outputState = tx.getOutput(0) as HouseState
        outputState.apply {
            require(outputState.valuationOfHouse.quantity > 0) {"Valuation cannot be zero"}
        }
    }

    override fun additionalUpdateChecks(tx: LedgerTransaction) {
        // Write contract validation logic to be performed while updation of token
    }

}