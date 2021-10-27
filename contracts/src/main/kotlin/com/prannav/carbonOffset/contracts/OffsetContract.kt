package com.prannav.carbonOffset.contracts

import com.r3.corda.lib.tokens.contracts.EvolvableTokenContract
import net.corda.core.contracts.Contract
import net.corda.core.contracts.Requirements
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction
import com.prannav.carbonOffset.states.HouseState
import com.prannav.carbonOffset.states.OffsetTokenState


class OffsetContract : EvolvableTokenContract(), Contract {
    companion object {
        const val CONTRACT_ID = "com.prannav.carbonOffset.contracts.OffsetContract"
    }
    override fun additionalCreateChecks(tx: LedgerTransaction) {
        // Write contract validation logic to be performed while creation of token
        val outputState = tx.getOutput(0) as OffsetTokenState
        outputState.apply {
            require(outputState.offsetPrice.quantity > 0) {"Offsets can be of value 0"}

        }
    }

    override fun additionalUpdateChecks(tx: LedgerTransaction) {
        // Write contract validation logic to be performed while updation of token
    }
}
