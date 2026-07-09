package boo.bloodstone.bloodrp

import org.bukkit.entity.Player

object RequestManager {
    private val pendingRequests: MutableMap<Player, ActionRequest> = mutableMapOf()

    fun getPendingRequest(player: Player): ActionRequest? {
        return pendingRequests[player]
    }

    fun setPendingPartner(partner: Player, sender: Player, action: Action) {
        action.notify(sender, partner)
        pendingRequests[partner] = ActionRequest(sender, action)
    }

    fun removeRequestFrom(player: Player) {
        pendingRequests.remove(player)
    }
}
