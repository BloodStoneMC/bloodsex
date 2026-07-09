package boo.bloodstone.bloodsex.commands

import io.papermc.paper.command.brigadier.Commands

object BloodRPCommandRegistrar {
    fun register(commands: Commands) {
        commands.register(RequestActionCommand("bj").node().build(), "Предложить bj")
        commands.register(
            RequestActionCommand("doggy").node().build(),
            "Предложить doggy",
            listOf("sex")
        )
        commands.register(RequestActionCommand("marry").node().build(), "Предложить свадьбу")
        commands.register(RapeCommand.node().build(), "Выполнить действие без подтверждения")
        commands.register(
            Commands.literal("bloodrp")
                .then(AcceptCommand.node())
                .build(),
            "BloodRP commands"
        )
    }
}
