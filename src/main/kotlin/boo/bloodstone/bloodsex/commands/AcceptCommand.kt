package boo.bloodstone.bloodsex.commands

import boo.bloodstone.bloodsex.BloodRP
import boo.bloodstone.bloodsex.PARTNER_ARGUMENT
import boo.bloodstone.bloodsex.RequestManager
import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import org.bukkit.entity.Player

object AcceptCommand : BloodRPCommand {
    override fun node(): LiteralArgumentBuilder<CommandSourceStack> =
        Commands.literal("accept")
            .requires { it.sender is Player && it.sender.hasPermission(BloodRPPermissions.SEX) }
            .then(
                Commands.argument(PARTNER_ARGUMENT, ArgumentTypes.player())
                    .executes { context -> execute(context) }
            )

    private fun execute(context: CommandContext<CommandSourceStack>): Int {
        val sender = context.source.sender as? Player ?: return 0
        val requester = context.getArgument(PARTNER_ARGUMENT, PlayerSelectorArgumentResolver::class.java)
            .resolve(context.source)
            .firstOrNull() ?: return 0
        val request = RequestManager.getPendingRequest(sender) ?: return 0

        val isTooFar = sender.location.world != requester.location.world ||
            sender.location.distance(requester.location) > BloodRP.config.maxActionDistance

        if (isTooFar) {
            sender.sendMessage("Вы слишком далеко от партнера")
            return 0
        }

        if (request.requester != requester) {
            return 0
        }

        request.action.play(requester, sender)
        RequestManager.removeRequestFrom(sender)
        return Command.SINGLE_SUCCESS
    }
}
