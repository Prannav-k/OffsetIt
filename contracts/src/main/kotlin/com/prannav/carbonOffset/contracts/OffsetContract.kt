package com.prannav.carbonOffset.contracts

import com.r3.corda.lib.tokens.contracts.EvolvableTokenContract
import net.corda.core.contracts.Contract
import net.corda.core.contracts.Requirements
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction
import com.prannav.carbonOffset.states.OffsetTokenState
import net.corda.core.contracts.Requirements.using


class OffsetContract : EvolvableTokenContract(), Contract {
    companion object {
        const val CONTRACT_ID = "com.prannav.carbonOffset.contracts.OffsetContract"
    }
    override fun additionalCreateChecks(tx: LedgerTransaction) {
        val outputState = tx.getOutput(0) as OffsetTokenState
        outputState.apply {
            require(outputState.offsetPrice.quantity > 0) {"Offsets cannot be of value 0"}
            require(outputState.expiryDays > 0) {"Offsets expiry days cannot less than of 0 days"}
            require(outputState.expiryDays < 1096) {"Offsets expiry days can't be more than 1-95 days (3 years) "}

            //Validate mandatory data
            require(outputState.offsetType.isNotBlank()) {"OffsetType can't be blank"}
            require(outputState.offsetUnit.isNotBlank()) {"offsetUnit can't be blank"}
            require(outputState.source.isNotBlank()) {"Source can't be blank"}
        }
    }

    override fun additionalUpdateChecks(tx: LedgerTransaction) {
        val outputState = tx.getOutput(0) as OffsetTokenState
        val inputState = tx.getInput(0) as OffsetTokenState

        outputState.apply {offsetType
            require(inputState.source != outputState.source) {"Source value cannot be changed"}
            require(inputState.offsetType != outputState.offsetType) {"offsetType value cannot be changed"}
            require(inputState.offsetUnit != outputState.offsetUnit) {"offsetUnit value cannot be changed"}
            require(inputState.expiryDays != outputState.expiryDays) {"expiryDays value cannot be changed"}

            //Validate mandatory data
            require(outputState.offsetType.isNotBlank()) {"OffsetType can't be blank"}
            require(outputState.offsetUnit.isNotBlank()) {"offsetUnit can't be blank"}
            require(outputState.source.isNotBlank()) {"Source can't be blank"}
        }
    }
}
