package boo.bloodstone.bloodsex

import boo.bloodstone.bloodsex.animations.blowjob
import com.github.trard.Scheduler
import org.bukkit.command.Command
import org.bukkit.entity.Player

class AcceptCommand(private val requestManager: RequestManager, private val scheduler: Scheduler): RequestCommandExecutor(requestManager) {
    override fun onRequest(firstPlayer: Player, secondPlayer: Player, command: Command) {
        val (partner, action) = requestManager.getPendingPartner(firstPlayer) ?: return

        if (firstPlayer.location.world != secondPlayer.location.world || firstPlayer.location.distance(secondPlayer.location) > 16.0) {
            firstPlayer.sendMessage("Вы слишком далеко от партнера")
            return
        }

        if (partner == secondPlayer) {
            action.play(firstPlayer, secondPlayer, scheduler)
        }

        requestManager.removeRequestFrom(firstPlayer)
    }
}