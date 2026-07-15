package boo.bloodstone.bloodrp.commands

import boo.bloodstone.bloodrp.BloodRP
import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import net.luckperms.api.LuckPerms
import net.luckperms.api.model.data.DataMutateResult
import net.luckperms.api.node.types.InheritanceNode
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.logging.Level

class PoliceGroupCommand(
    private val luckPerms: LuckPerms,
    private val commandName: String,
    private val grant: Boolean,
) : BloodRPCommand {
    override fun node(): LiteralArgumentBuilder<CommandSourceStack> =
        Commands.literal(commandName)
            .requires { it.sender.hasPermission(BloodRPPermissions.POLICE) }
            .then(
                Commands.argument(PLAYER_ARGUMENT, StringArgumentType.word())
                    .executes { context -> execute(context) }
            )

    private fun execute(context: CommandContext<CommandSourceStack>): Int {
        val sender = context.source.sender
        val playerName = StringArgumentType.getString(context, PLAYER_ARGUMENT)
        val groupName = BloodRP.config.policeGroup

        BloodRP.coroutineScope.launch {
            try {
                updatePoliceGroup(sender, playerName, groupName)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                BloodRP.plugin.logger.log(
                    Level.WARNING,
                    "Failed to execute /bloodrp $commandName for $playerName.",
                    exception,
                )
                sendMessage(sender, "Не удалось изменить группу игрока. Проверьте консоль.")
            }
        }

        return Command.SINGLE_SUCCESS
    }

    private suspend fun updatePoliceGroup(sender: CommandSender, playerName: String, groupName: String) {
        if (groupName.isBlank()) {
            sendMessage(sender, "Группа полиции не настроена в config.yml.")
            return
        }

        val group = luckPerms.groupManager.getGroup(groupName)
            ?: luckPerms.groupManager.loadGroup(groupName).await().orElse(null)
        if (group == null) {
            sendMessage(sender, "Группа LuckPerms '$groupName' не найдена.")
            return
        }

        val userManager = luckPerms.userManager
        val playerId = try {
            userManager.lookupUniqueId(playerName).await()
        } catch (_: IllegalArgumentException) {
            sendMessage(sender, "Некорректный ник игрока: $playerName")
            return
        }

        if (playerId == null) {
            sendMessage(sender, "Игрок $playerName не найден.")
            return
        }

        val user = userManager.loadUser(playerId).await()
        try {
            val targetName = user.username ?: playerName
            val groupNode = InheritanceNode.builder(group).build()
            val mutationResult = if (grant) {
                user.data().add(groupNode)
            } else {
                user.data().remove(groupNode)
            }

            if (mutationResult == DataMutateResult.SUCCESS) {
                userManager.saveUser(user).await()
            }

            sendMutationResult(sender, targetName, groupName, mutationResult)
        } finally {
            userManager.cleanupUser(user)
        }
    }

    private fun sendMutationResult(
        sender: CommandSender,
        playerName: String,
        groupName: String,
        result: DataMutateResult,
    ) {
        val message = when (result) {
            DataMutateResult.SUCCESS -> if (grant) {
                "Игроку $playerName выдана группа $groupName."
            } else {
                "У игрока $playerName забрана группа $groupName."
            }

            DataMutateResult.FAIL_ALREADY_HAS -> "Игрок $playerName уже состоит в группе $groupName."
            DataMutateResult.FAIL_LACKS -> "Игрок $playerName не состоит в группе $groupName."
            DataMutateResult.FAIL -> "LuckPerms не смог изменить группу игрока $playerName."
        }

        sendMessage(sender, message)
    }

    private fun sendMessage(sender: CommandSender, message: String) {
        if (sender is Player) {
            BloodRP.scheduler.runAtEntity(sender, null) {
                sender.sendMessage(message)
            }
            return
        }

        BloodRP.scheduler.runGlobal {
            sender.sendMessage(message)
        }
    }

    private companion object {
        const val PLAYER_ARGUMENT = "player"
    }
}
