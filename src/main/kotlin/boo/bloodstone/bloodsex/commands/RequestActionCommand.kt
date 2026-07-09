package boo.bloodstone.bloodsex.commands

import boo.bloodstone.bloodsex.ActionMaster
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

class RequestActionCommand(private val actionName: String) : BloodRPCommand {
    override fun node(): LiteralArgumentBuilder<CommandSourceStack> =
        Commands.literal(actionName)
            .requires { it.sender is Player && it.sender.hasPermission(BloodRPPermissions.SEX) }
            .then(
                Commands.argument(PARTNER_ARGUMENT, ArgumentTypes.player())
                    .executes { context -> execute(context) }
            )

    private fun execute(context: CommandContext<CommandSourceStack>): Int {
        val sender = context.source.sender as? Player ?: return 0
        val partner = context.getArgument(PARTNER_ARGUMENT, PlayerSelectorArgumentResolver::class.java)
            .resolve(context.source)
            .firstOrNull() ?: return 0

        if (partner == sender) {
            sender.sendMessage("Самоотсос?")
            return 0
        }

        val action = ActionMaster.fromName(actionName) ?: return 0
        RequestManager.setPendingPartner(partner, sender, action)
        return Command.SINGLE_SUCCESS
    }
}
