package boo.bloodstone.bloodrp

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class MarriageKissCooldownTest {
    @Test
    fun `cooldown is shared by both orders of a marriage pair`() {
        val clock = MutableClock(Instant.parse("2026-07-12T00:00:00Z"))
        val cooldowns = MarriageKissCooldown(clock)
        val firstPlayerId = UUID.randomUUID()
        val secondPlayerId = UUID.randomUUID()
        val cooldown = Duration.ofSeconds(5)

        assertTrue(cooldowns.tryAcquire(firstPlayerId, secondPlayerId, cooldown))
        assertFalse(cooldowns.tryAcquire(secondPlayerId, firstPlayerId, cooldown))
        clock.advance(Duration.ofSeconds(5))
        assertTrue(cooldowns.tryAcquire(secondPlayerId, firstPlayerId, cooldown))
    }

    @Test
    fun `only one concurrent toggle acquires a pair cooldown`() {
        val cooldowns = MarriageKissCooldown(MutableClock(Instant.parse("2026-07-12T00:00:00Z")))
        val firstPlayerId = UUID.randomUUID()
        val secondPlayerId = UUID.randomUUID()
        val attempts = 16
        val executor = Executors.newFixedThreadPool(attempts)
        val ready = CountDownLatch(attempts)
        val start = CountDownLatch(1)

        try {
            val futures = List(attempts) {
                executor.submit<Boolean> {
                    ready.countDown()
                    check(start.await(10, TimeUnit.SECONDS))
                    cooldowns.tryAcquire(firstPlayerId, secondPlayerId, Duration.ofSeconds(5))
                }
            }
            assertTrue(ready.await(10, TimeUnit.SECONDS))
            start.countDown()

            assertEquals(1, futures.count { future -> future.get(10, TimeUnit.SECONDS) })
        } finally {
            executor.shutdownNow()
        }
    }
}
