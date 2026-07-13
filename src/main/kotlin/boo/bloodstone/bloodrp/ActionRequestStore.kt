package boo.bloodstone.bloodrp

import java.time.Clock
import java.time.Duration
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class ActionRequestStore(
    private val clock: Clock = Clock.systemUTC(),
    private val requestLifetime: Duration = Duration.ofMinutes(1),
) {
    private val requests = ConcurrentHashMap<UUID, ActionRequest>()

    init {
        require(!requestLifetime.isNegative && !requestLifetime.isZero) { "Request lifetime must be positive" }
    }

    fun create(requesterId: UUID, recipientId: UUID, action: Action): ActionRequest {
        val request = ActionRequest(
            UUID.randomUUID(),
            requesterId,
            recipientId,
            action,
            clock.instant().plus(requestLifetime)
        )
        check(requests.putIfAbsent(request.id, request) == null) { "Request ID collision: ${request.id}" }
        return request
    }

    fun get(requestId: UUID, recipientId: UUID): ActionRequest? {
        val request = requests[requestId] ?: return null
        if (request.recipientId != recipientId) return null
        if (request.expiresAt.isAfter(clock.instant())) return request

        requests.remove(requestId, request)
        return null
    }

    fun consume(requestId: UUID, recipientId: UUID): ActionRequest? {
        val request = get(requestId, recipientId) ?: return null
        return request.takeIf { requests.remove(requestId, request) }
    }
}
