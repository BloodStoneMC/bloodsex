package boo.bloodstone.bloodsex

import boo.bloodstone.commonBloodLib.Scheduler
import org.bukkit.command.Command
import org.bukkit.entity.Player

class AcceptCommand(private val requestManager: RequestManager, private val scheduler: Scheduler): RequestCommandExecutor(requestManager) {
    override fun onRequest(firstPlayer: Player, secondPlayer: Player, command: Command) {
        val (partner, action) = requestManager.getPendingPartner(firstPlayer) ?: return

        if (!firstPlayer.isCloseEnoughTo(secondPlayer)) {
            firstPlayer.sendMessage("Вы слишком далеко от партнера")
            return
        }

        if (partner == secondPlayer) {
            action.play(firstPlayer, secondPlayer, scheduler)
        }

        requestManager.removeRequestFrom(firstPlayer)
    }
}
