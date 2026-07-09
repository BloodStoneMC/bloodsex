package boo.bloodstone.bloodsex.commands

import boo.bloodstone.bloodsex.RequestManager
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack

interface BloodRPCommand {
    fun node(requestManager: RequestManager): LiteralArgumentBuilder<CommandSourceStack>
}
