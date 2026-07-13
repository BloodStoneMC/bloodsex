package boo.bloodstone.bloodrp

import org.bukkit.entity.Player
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant
import java.util.UUID

class ActionRequestStoreTest {
    @Test
    fun `old request id consumes its original action instead of a newer request`() {
        val requesterId = UUID.randomUUID()
        val recipientId = UUID.randomUUID()
        val store = ActionRequestStore(
            clock = MutableClock(Instant.parse("2026-07-12T00:00:00Z")),
            requestLifetime = Duration.ofMinutes(1),
        )
        val oldAction = action()
        val newAction = action()

        val oldRequest = store.create(requesterId, recipientId, oldAction)
        val newRequest = store.create(requesterId, recipientId, newAction)

        assertSame(oldAction, store.consume(oldRequest.id, recipientId)?.action)
        assertSame(newAction, store.consume(newRequest.id, recipientId)?.action)
    }

    @Test
    fun `request remains bound to its recipient and expiration`() {
        val clock = MutableClock(Instant.parse("2026-07-12T00:00:00Z"))
        val recipientId = UUID.randomUUID()
        val store = ActionRequestStore(clock, Duration.ofSeconds(30))
        val request = store.create(UUID.randomUUID(), recipientId, action())

        assertNull(store.consume(request.id, UUID.randomUUID()))
        clock.advance(Duration.ofSeconds(30))
        assertNull(store.consume(request.id, recipientId))
    }

    private fun action() = object : Action {
        override fun notify(firstPlayer: Player, secondPlayer: Player, request: ActionRequest) = Unit
        override fun play(firstPlayer: Player, secondPlayer: Player) = Unit
    }
}
