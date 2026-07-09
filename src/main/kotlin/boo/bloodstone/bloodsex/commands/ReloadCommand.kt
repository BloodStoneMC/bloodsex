package boo.bloodstone.bloodsex.commands

import boo.bloodstone.bloodsex.BloodRP
import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import java.util.logging.Level

object ReloadCommand : BloodRPCommand {
    override fun node(): LiteralArgumentBuilder<CommandSourceStack> =
        Commands.literal("reload")
            .requires { it.sender.hasPermission(BloodRPPermissions.RELOAD) }
            .executes { context -> execute(context.source) }

    private fun execute(source: CommandSourceStack): Int {
        try {
            BloodRP.plugin.reloadRuntimeConfig()
        } catch (exception: Exception) {
            BloodRP.plugin.logger.log(Level.WARNING, "Failed to reload BloodRP config.", exception)
            source.sender.sendMessage(
                Component.text("BloodRP config reload failed. Check server logs.", NamedTextColor.RED)
            )
            return 0
        }

        source.sender.sendMessage(Component.text("BloodRP config reloaded.", NamedTextColor.GREEN))
        return Command.SINGLE_SUCCESS
    }
}
