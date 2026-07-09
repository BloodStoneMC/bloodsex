package boo.bloodstone.bloodsex

import org.bukkit.entity.Player

class RequestManager {
    private val pendingRequests: MutableMap<Player, Pair<Player, Action>> = mutableMapOf()

    fun getPendingPartner(player: Player): Pair<Player, Action>? {
        return pendingRequests[player]
    }

    fun setPendingPartner(partner: Player, sender: Player, action: Action) {
        action.notify(sender, partner)
        pendingRequests[partner] = Pair(sender, action)
    }

    fun removeRequestFrom(player: Player) {
        pendingRequests.remove(player)
    }
}
