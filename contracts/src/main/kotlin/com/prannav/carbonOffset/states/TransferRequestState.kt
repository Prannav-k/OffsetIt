package com.prannav.carbonOffset.states

import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import com.prannav.carbonOffset.contracts.TransferRequestContract


@BelongsToContract(TransferRequestContract::class)
data class TransferRequestState(
                                val requestFrom: Party,
                                val requestTo: Party,
                                val status : String,
                                override val linearId: UniqueIdentifier = UniqueIdentifier()): LinearState {
    override val participants: List<AbstractParty> get() = listOf(requestFrom, requestTo)
    fun withNewStatus(newStatus: String) = copy(status = newStatus)

}