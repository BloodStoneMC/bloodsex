package boo.bloodstone.bloodsex.commands

import io.papermc.paper.command.brigadier.Commands

object BloodRPCommandRegistrar {
    fun register(commands: Commands) {
        commands.register(RequestActionCommand("bj").node().build(), "Предложить bj")
        commands.register(RequestActionCommand("kiss").node().build(), "Предложить поцелуй")
        commands.register(
            RequestActionCommand("doggy").node().build(),
            "Предложить doggy",
            listOf("sex")
        )
        commands.register(MarryCommand().node().build(), "Предложить свадьбу")
        commands.register(RapeCommand.node().build(), "Выполнить действие без подтверждения")
        commands.register(
            Commands.literal("bloodrp")
                .then(AcceptCommand.node())
                .then(ReloadCommand.node())
                .build(),
            "BloodRP commands"
        )
    }
}
