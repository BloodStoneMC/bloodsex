package boo.bloodstone.bloodsex

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

abstract class RequestCommandExecutor(private val requestManager: RequestManager): CommandExecutor {
    abstract fun onRequest(firstPlayer: Player, secondPlayer: Player, command: Command)

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (sender !is Player) return false
        if (args?.first().isNullOrEmpty()) return false

        val nickname = args?.first() ?: return false

        val player = Bukkit.getPlayer(nickname)

        if (player == sender) {
            sender.sendMessage("Самоотсос?")
            return false
        }

        if (player == null) {
            sender.sendMessage("Неверный ник или игрок оффлайн")
            return false
        }

        onRequest(sender, player, command)

        return true
    }
}