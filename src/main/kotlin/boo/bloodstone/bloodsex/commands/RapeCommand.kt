package boo.bloodstone.bloodsex.commands

import boo.bloodstone.bloodsex.ActionMaster
import boo.bloodstone.bloodsex.RequestManager
import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import org.bukkit.entity.Player

object RapeCommand : BloodRPCommand {
    override fun node(requestManager: RequestManager): LiteralArgumentBuilder<CommandSourceStack> {
        val root = Commands.literal("rape")
            .requires { it.sender is Player && it.sender.hasPermission(BloodRPPermissions.RAPE) }

        for (actionName in DIRECT_ACTION_NAMES) {
            root.then(
                Commands.literal(actionName)
                    .then(
                        Commands.argument(PARTNER_ARGUMENT, ArgumentTypes.player())
                            .executes { context -> execute(context, actionName) }
                    )
            )
        }

        return root
    }

    private fun execute(context: CommandContext<CommandSourceStack>, actionName: String): Int {
        val sender = context.source.sender as? Player ?: return 0
        val partner = context.getArgument(PARTNER_ARGUMENT, PlayerSelectorArgumentResolver::class.java)
            .resolve(context.source)
            .firstOrNull() ?: return 0

        if (partner == sender) {
            sender.sendMessage("Самоотсос?")
            return 0
        }

        if (partner.hasPermission(BloodRPPermissions.RAPE_IMMUNE)) {
            sender.sendMessage("Этого игрока нельзя изнасиловать")
            return 0
        }

        val isTooFar = sender.location.world != partner.location.world ||
            sender.location.distance(partner.location) > MAX_RAPE_DISTANCE

        if (isTooFar) {
            sender.sendMessage("Вы слишком далеко от партнера")
            return 0
        }

        val action = ActionMaster.fromName(actionName) ?: return 0
        if (actionName == "bj") {
            action.play(sender, partner)
        } else {
            action.play(partner, sender)
        }
        return Command.SINGLE_SUCCESS
    }

    private val DIRECT_ACTION_NAMES = listOf("bj", "doggy")
}
