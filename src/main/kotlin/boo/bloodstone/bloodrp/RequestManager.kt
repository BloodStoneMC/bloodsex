package boo.bloodstone.bloodrp

import org.bukkit.entity.Player
import java.util.UUID

object RequestManager {
    private val requests = ActionRequestStore()

    fun getPendingRequest(requestId: UUID, recipientId: UUID) = requests.get(requestId, recipientId)

    fun setPendingPartner(partner: Player, sender: Player, action: Action) {
        action.notify(sender, partner, requests.create(sender.uniqueId, partner.uniqueId, action))
    }

    fun consumePendingRequest(requestId: UUID, recipientId: UUID) = requests.consume(requestId, recipientId)
}
