package boo.bloodstone.bloodrp.commands

import boo.bloodstone.bloodOfflinePlayersAPI.MessageClient as OfflinePlayersClient
import boo.bloodstone.bloodrp.BloodRP
import boo.bloodstone.bloodrp.database.MarriagesTable
import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.UUID
import java.util.logging.Level
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toJavaUuid

class MarryTopCommand : BloodRPCommand {
    private val miniMessage = MiniMessage.miniMessage()
    private val offlinePlayersClient = OfflinePlayersClient()

    override fun node(): LiteralArgumentBuilder<CommandSourceStack> =
        Commands.literal("top")
            .executes { context -> execute(context.source) }

    private fun execute(source: CommandSourceStack): Int {
        val sender = source.sender

        BloodRP.coroutineScope.launch {
            val messages = runCatching { buildMessages() }.getOrElse { exception ->
                BloodRP.plugin.logger.log(Level.WARNING, "Failed to load marriage top.", exception)
                listOf(Component.text("Не удалось загрузить топ браков", NamedTextColor.RED))
            }

            sendMessages(sender, messages)
        }

        return Command.SINGLE_SUCCESS
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun buildMessages(): List<Component> {
        val entries = loadTopMarriages()
        val namesByUuid = resolveNames(entries.flatMap { listOf(it.husband, it.wife) }.toSet())
        val now = Clock.System.now()

        return buildList {
            add(miniMessage.deserialize(BloodRP.config.topMarriagesHeader))

            if (entries.isEmpty()) {
                add(Component.text("Браков пока нет"))
            } else {
                addAll(entries.mapIndexed { index, entry -> entryLine(index + 1, entry, namesByUuid, now) })
            }

            add(miniMessage.deserialize(BloodRP.config.topMarriagesFooter))
        }
    }

    @OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)
    private fun loadTopMarriages(): List<MarriageTopEntry> = transaction(readOnly = true) {
        MarriagesTable
            .select(MarriagesTable.husband, MarriagesTable.wife, MarriagesTable.startedAt)
            .orderBy(MarriagesTable.startedAt, SortOrder.ASC)
            .limit(TOP_LIMIT)
            .map { row ->
                MarriageTopEntry(
                    husband = row[MarriagesTable.husband].toJavaUuid(),
                    wife = row[MarriagesTable.wife].toJavaUuid(),
                    startedAt = row[MarriagesTable.startedAt],
                )
            }
    }

    private suspend fun resolveNames(uuids: Set<UUID>): Map<UUID, String> {
        if (uuids.isEmpty()) {
            return emptyMap()
        }

        return try {
            offlinePlayersClient.getProfiles(uuids).filterNotNull().associate { it.uuid to it.name }
        } catch (exception: Exception) {
            BloodRP.plugin.logger.log(Level.WARNING, "Failed to resolve marriage top player names.", exception)
            emptyMap()
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun entryLine(
        index: Int,
        entry: MarriageTopEntry,
        namesByUuid: Map<UUID, String>,
        now: Instant,
    ): Component {
        val husbandName = namesByUuid[entry.husband] ?: entry.husband.toString()
        val wifeName = namesByUuid[entry.wife] ?: entry.wife.toString()
        val duration = formatDuration(now - entry.startedAt)

        return Component.text("$index. $husbandName и $wifeName женаты уже $duration")
    }

    private fun formatDuration(duration: Duration): String {
        val totalHours = maxOf(0, duration.inWholeHours)
        val days = totalHours / HOURS_IN_DAY
        val hours = totalHours % HOURS_IN_DAY

        return "$days д. $hours ч."
    }

    private fun sendMessages(sender: CommandSender, messages: List<Component>) {
        if (sender is Player) {
            BloodRP.scheduler.runAtEntity(sender, null) {
                messages.forEach(sender::sendMessage)
            }
            return
        }

        BloodRP.scheduler.runGlobal {
            messages.forEach(sender::sendMessage)
        }
    }

    @OptIn(ExperimentalTime::class)
    private data class MarriageTopEntry(
        val husband: UUID,
        val wife: UUID,
        val startedAt: Instant,
    )

    private companion object {
        const val TOP_LIMIT = 10
        const val HOURS_IN_DAY = 24
    }
}
