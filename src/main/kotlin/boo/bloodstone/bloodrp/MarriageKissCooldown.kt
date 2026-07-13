package boo.bloodstone.bloodrp

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class MarriageKissCooldown(
    private val clock: Clock = Clock.systemUTC(),
) {
    private val blockedUntil = ConcurrentHashMap<Set<UUID>, Instant>()

    fun tryAcquire(firstPlayerId: UUID, secondPlayerId: UUID, duration: Duration): Boolean {
        if (firstPlayerId == secondPlayerId) return false
        if (duration.isZero || duration.isNegative) return true

        val now = clock.instant()
        var acquired = false
        blockedUntil.compute(setOf(firstPlayerId, secondPlayerId)) { _, current ->
            if (current?.isAfter(now) == true) current else now.plus(duration).also { acquired = true }
        }
        return acquired
    }
}
