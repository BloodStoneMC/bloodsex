package boo.bloodstone.bloodrp

import java.time.Instant
import java.util.UUID

data class ActionRequest(
    val id: UUID,
    val requesterId: UUID,
    val recipientId: UUID,
    val action: Action,
    val expiresAt: Instant,
)
