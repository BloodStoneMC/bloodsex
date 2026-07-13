package boo.bloodstone.bloodrp.database

import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class MarriageRepositoryConcurrencyTest {
    @Test
    @OptIn(ExperimentalTime::class)
    fun `concurrent marriages sharing one player produce one database row`() {
        val databaseName = "marriage_${UUID.randomUUID().toString().replace("-", "")}"
        Database.connect("jdbc:h2:mem:$databaseName;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
        MarriageRepository.initializeSchema()

        val sharedPlayerId = UUID.randomUUID()
        val partnerIds = List(8) { UUID.randomUUID() }
        val executor = Executors.newFixedThreadPool(partnerIds.size)
        val ready = CountDownLatch(partnerIds.size)
        val start = CountDownLatch(1)

        try {
            val futures = partnerIds.map { partnerId ->
                executor.submit<Set<UUID>> {
                    ready.countDown()
                    check(start.await(10, TimeUnit.SECONDS))
                    MarriageRepository.createMarriage(
                        sharedPlayerId,
                        partnerId,
                        Instant.parse("2026-07-12T00:00:00Z")
                    )
                }
            }
            assertTrue(ready.await(10, TimeUnit.SECONDS))
            start.countDown()
            val results = futures.map { future -> future.get(10, TimeUnit.SECONDS) }

            assertEquals(1, results.count { it.isEmpty() })
            assertEquals(partnerIds.size - 1, results.count { it.isNotEmpty() })
            results.filter { it.isNotEmpty() }.forEach { marriedPlayerIds ->
                assertTrue(sharedPlayerId in marriedPlayerIds)
            }

            val rowCounts = transaction(readOnly = true) {
                MarriagesTable.selectAll().count() to MarriageParticipantsTable.selectAll().count()
            }
            assertEquals(1L, rowCounts.first)
            assertEquals(2L, rowCounts.second)
        } finally {
            executor.shutdownNow()
        }
    }
}
