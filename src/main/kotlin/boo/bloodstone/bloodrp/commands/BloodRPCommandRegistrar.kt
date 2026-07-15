package boo.bloodstone.bloodrp.commands

import io.papermc.paper.command.brigadier.Commands
import net.luckperms.api.LuckPerms

object BloodRPCommandRegistrar {
    fun register(commands: Commands, luckPerms: LuckPerms) {
        commands.register(RequestActionCommand("bj").node().build(), "Предложить bj")
        commands.register(RequestActionCommand("kiss").node().build(), "Предложить поцелуй")
        commands.register(
            RequestActionCommand("doggy").node().build(),
            "Предложить doggy",
            listOf("sex")
        )
        commands.register(MarryCommand().node().build(), "Предложить свадьбу")
        commands.register(DivorceCommand().node().build(), "Развестись", listOf("divorce"))
        commands.register(RapeCommand.node().build(), "Выполнить действие без подтверждения")
        commands.register(
            Commands.literal("bloodrp")
                .then(AcceptCommand.node())
                .then(
                    PoliceGroupCommand(
                        luckPerms = luckPerms,
                        commandName = "police",
                        grant = true,
                    ).node()
                )
                .then(
                    PoliceGroupCommand(
                        luckPerms = luckPerms,
                        commandName = "unpolice",
                        grant = false,
                    ).node()
                )
                .then(ReloadCommand.node())
                .build(),
            "BloodRP commands"
        )
    }
}
