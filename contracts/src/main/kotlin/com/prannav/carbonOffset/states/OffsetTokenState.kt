package com.prannav.carbonOffset.states

import com.r3.corda.lib.tokens.contracts.states.EvolvableTokenType
import net.corda.core.contracts.Amount
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party

import com.prannav.carbonOffset.contracts.OffsetContract
import java.util.*


@BelongsToContract(OffsetContract::class)
class OffsetTokenState(override val linearId: UniqueIdentifier,
                       override val maintainers: List<Party>,
                       val offsetPrice: Amount<Currency>,
                       val offsetType: String,
                       val offsetUnit: String,
                       val source: String,
                       val otherInfo : String,
                       val expiryDays : Int,
                       val issuer:Party = maintainers.single(),
                       override val fractionDigits: Int = 0
) : EvolvableTokenType()