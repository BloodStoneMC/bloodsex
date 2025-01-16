package boo.bloodstone.bloodsex

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Color
import org.bukkit.entity.Player

class RequestManager {
    private val pendingRequests: MutableMap<Player, Pair<Player, Action>> = mutableMapOf()

    fun getPendingPartner(player: Player): Pair<Player, Action>? {
        return pendingRequests[player]
    }

    fun setPendingPartner(partner: Player, sender: Player, action: Action) {
        sender.sendMessage("Вы отправили предложение ${partner.name}")
        partner.sendMessage("${sender.name} ${action.request}")
        partner.sendMessage(Component.text("[Принять]").color(NamedTextColor.GREEN).clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/accept ${sender.name}")))
        pendingRequests[partner] = Pair(sender, action)
    }

    fun removeRequestFrom(player: Player) {
        pendingRequests.remove(player)
    }
}