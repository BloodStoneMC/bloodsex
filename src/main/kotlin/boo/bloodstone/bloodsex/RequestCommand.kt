package boo.bloodstone.bloodsex

import org.bukkit.command.Command
import org.bukkit.entity.Player

class RequestCommand(private val requestManager: RequestManager): RequestCommandExecutor(requestManager) {
    override fun onRequest(firstPlayer: Player, secondPlayer: Player, command: Command) {
        val action = Action.fromName(command.name) ?: return

        requestManager.setPendingPartner(secondPlayer, firstPlayer, action)
    }
}