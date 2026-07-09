package boo.bloodstone.bloodsex.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack

interface BloodRPCommand {
    fun node(): LiteralArgumentBuilder<CommandSourceStack>
}
