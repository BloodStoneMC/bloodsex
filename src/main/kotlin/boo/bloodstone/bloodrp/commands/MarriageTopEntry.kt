package boo.bloodstone.bloodrp.commands

import java.util.UUID
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
data class MarriageTopEntry(
    val husband: UUID,
    val wife: UUID,
    val startedAt: Instant,
)
