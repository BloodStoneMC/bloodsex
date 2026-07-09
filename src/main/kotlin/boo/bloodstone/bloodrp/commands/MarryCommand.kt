package boo.bloodstone.bloodrp.commands

import boo.bloodstone.bloodrp.ActionMaster
import boo.bloodstone.bloodrp.PARTNER_ARGUMENT
import boo.bloodstone.bloodrp.RequestManager
import boo.bloodstone.bloodrp.animations.MarryAnimation
import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import org.bukkit.entity.Player

class MarryCommand : BloodRPCommand {
    override fun node(): LiteralArgumentBuilder<CommandSourceStack> =
        Commands.literal(ACTION_NAME)
            .requires { it.sender is Player && it.sender.hasPermission(BloodRPPermissions.SEX) }
            .then(MarryTopCommand().node())
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

        if (!canMarry(sender, partner)) {
            return 0
        }

        val action = ActionMaster.fromName(ACTION_NAME) ?: return 0
        RequestManager.setPendingPartner(partner, sender, action)
        return Command.SINGLE_SUCCESS
    }

    private fun canMarry(sender: Player, partner: Player): Boolean {
        val marriedPlayers = MarryAnimation.getMarriedPlayers(sender, partner)

        if (marriedPlayers.isEmpty()) {
            return true
        }

        val message = marriedPlayers.joinToString("\n") { "${it.name} уже в браке" }
        sender.sendMessage(message)
        partner.sendMessage(message)
        return false
    }

    private companion object {
        const val ACTION_NAME = "marry"
    }
}
