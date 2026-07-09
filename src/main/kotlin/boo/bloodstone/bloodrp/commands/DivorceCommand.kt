package boo.bloodstone.bloodrp.commands

import boo.bloodstone.bloodrp.clearMarriagePartner
import boo.bloodstone.bloodrp.database.MarriagesTable
import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

class DivorceCommand : BloodRPCommand {
    override fun node(): LiteralArgumentBuilder<CommandSourceStack> =
        Commands.literal(COMMAND_NAME)
            .requires { it.sender is Player && it.sender.hasPermission(BloodRPPermissions.SEX) }
            .executes { context -> execute(context.source) }

    private fun execute(source: CommandSourceStack): Int {
        val sender = source.sender as? Player ?: return 0
        val partnerUuid = removeMarriage(sender)

        if (partnerUuid == null) {
            sender.sendMessage("Вы не состоите в браке")
            return 0
        }

        sender.clearMarriagePartner()
        sender.sendMessage("Вы развелись")

        val partner = Bukkit.getPlayer(partnerUuid) ?: return Command.SINGLE_SUCCESS
        partner.clearMarriagePartner()
        partner.sendMessage("${sender.name} развелся с вами")

        return Command.SINGLE_SUCCESS
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun removeMarriage(player: Player): UUID? {
        val playerUuid = player.uniqueId.toKotlinUuid()

        return transaction {
            val row = MarriagesTable
                .select(MarriagesTable.husband, MarriagesTable.wife)
                .where((MarriagesTable.husband eq playerUuid) or (MarriagesTable.wife eq playerUuid))
                .limit(1)
                .singleOrNull() ?: return@transaction null
            val partnerUuid = if (row[MarriagesTable.husband] == playerUuid) {
                row[MarriagesTable.wife]
            } else {
                row[MarriagesTable.husband]
            }

            MarriagesTable.deleteWhere {
                (MarriagesTable.husband eq playerUuid) or (MarriagesTable.wife eq playerUuid)
            }

            partnerUuid.toJavaUuid()
        }
    }

    private companion object {
        const val COMMAND_NAME = "unmarry"
    }
}
