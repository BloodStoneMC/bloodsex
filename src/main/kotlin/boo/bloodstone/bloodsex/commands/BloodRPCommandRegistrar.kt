package boo.bloodstone.bloodsex.commands

import boo.bloodstone.bloodsex.RequestManager
import io.papermc.paper.command.brigadier.Commands

object BloodRPCommandRegistrar {
    fun register(commands: Commands, requestManager: RequestManager) {
        commands.register(RequestActionCommand("bj").node(requestManager).build(), "Предложить bj")
        commands.register(
            RequestActionCommand("doggy").node(requestManager).build(),
            "Предложить doggy",
            listOf("sex")
        )
        commands.register(RequestActionCommand("marry").node(requestManager).build(), "Предложить свадьбу")
        commands.register(RapeCommand.node(requestManager).build(), "Выполнить действие без подтверждения")
        commands.register(
            Commands.literal("bloodrp")
                .then(AcceptCommand.node(requestManager))
                .build(),
            "BloodRP commands"
        )
    }
}
