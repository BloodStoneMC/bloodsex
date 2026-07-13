package boo.bloodstone.bloodrp.database

import java.util.UUID
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
data class PlayerMarriage(
    val partnerId: UUID,
    val lastInteractionAt: Instant,
)
