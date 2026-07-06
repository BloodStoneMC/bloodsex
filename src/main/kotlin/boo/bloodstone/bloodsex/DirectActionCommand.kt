package boo.bloodstone.bloodsex

import boo.bloodstone.commonBloodLib.Scheduler
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class DirectActionCommand(private val scheduler: Scheduler) : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) return false
        if (args.size < 2) return false

        val action = directActionFromName(args[0]) ?: return false
        val partner = Bukkit.getPlayer(args[1])

        if (partner == sender) {
            sender.sendMessage("Самоотсос?")
            return false
        }

        if (partner == null) {
            sender.sendMessage("Неверный ник или игрок оффлайн")
            return false
        }

        if (partner.hasPermission("bloodsex.rape.immune")) {
            sender.sendMessage("Этого игрока нельзя изнасиловать")
            return false
        }

        if (!sender.isCloseEnoughTo(partner, MAX_RAPE_DISTANCE)) {
            sender.sendMessage("Вы слишком далеко от партнера")
            return false
        }

        when (action) {
            Action.Bj -> action.play(sender, partner, scheduler)
            else -> action.play(partner, sender, scheduler)
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): List<String> {
        return when (args.size) {
            1 -> actionNames.filter { it.startsWith(args[0].lowercase()) }
            2 -> Bukkit.getOnlinePlayers()
                .map { it.name }
                .filter { it != sender.name && it.startsWith(args[1], ignoreCase = true) }
            else -> emptyList()
        }
    }

    private fun directActionFromName(name: String): Action? {
        return when (name.lowercase()) {
            "bj" -> Action.Bj
            "doggy" -> Action.Doggy
            else -> null
        }
    }

    private companion object {
        val actionNames = listOf("bj", "doggy")
    }
}
