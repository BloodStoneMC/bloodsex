package boo.bloodstone.bloodrp.commands

import boo.bloodstone.bloodrp.BloodRP
import boo.bloodstone.bloodrp.REQUEST_ARGUMENT
import boo.bloodstone.bloodrp.RequestManager
import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.UUID

object AcceptCommand : BloodRPCommand {
    override fun node(): LiteralArgumentBuilder<CommandSourceStack> =
        Commands.literal("accept")
            .requires { it.sender is Player && it.sender.hasPermission(BloodRPPermissions.SEX) }
            .then(
                Commands.argument(REQUEST_ARGUMENT, StringArgumentType.word())
                    .executes { context -> execute(context) }
            )

    private fun execute(context: CommandContext<CommandSourceStack>): Int {
        val sender = context.source.sender as? Player ?: return 0
        val requestId = runCatching {
            UUID.fromString(StringArgumentType.getString(context, REQUEST_ARGUMENT))
        }.getOrNull() ?: return 0
        val request = RequestManager.getPendingRequest(requestId, sender.uniqueId) ?: return 0
        val requester = Bukkit.getPlayer(request.requesterId) ?: return 0

        val isTooFar = sender.location.world != requester.location.world ||
            sender.location.distance(requester.location) > BloodRP.config.maxActionDistance

        if (isTooFar) {
            sender.sendMessage("Вы слишком далеко от партнера")
            return 0
        }

        val consumedRequest = RequestManager.consumePendingRequest(requestId, sender.uniqueId) ?: return 0
        consumedRequest.action.play(requester, sender)
        return Command.SINGLE_SUCCESS
    }
}
