package boo.bloodstone.bloodsex.listeners

import boo.bloodstone.bloodsex.BloodRP
import boo.bloodstone.bloodsex.clearMarriagePartner
import boo.bloodstone.bloodsex.database.MarriagesTable
import boo.bloodstone.bloodsex.marriagePartnerUuid
import kotlinx.coroutines.launch
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.UUID
import java.util.logging.Level
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toKotlinUuid

object MarriagePdcCleanupListener : Listener {
    @EventHandler
    @OptIn(ExperimentalTime::class)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        val playerUuid = player.uniqueId
        val partnerUuid = player.marriagePartnerUuid() ?: return

        BloodRP.coroutineScope.launch {
            val lastInteractionAt = try {
                lastInteractionAt(playerUuid, partnerUuid)
            } catch (exception: Exception) {
                BloodRP.plugin.logger.log(Level.WARNING, "Failed to verify marriage PDC for $playerUuid.", exception)
                return@launch
            }

            if (lastInteractionAt == null) {
                clearStaleMarriagePartner(player, partnerUuid)
            } else {
                sendInteractionWarningIfNeeded(player, partnerUuid, lastInteractionAt)
            }
        }
    }

    @OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)
    private fun lastInteractionAt(playerUuid: UUID, partnerUuid: UUID): Instant? {
        val playerKotlinUuid = playerUuid.toKotlinUuid()
        val partnerKotlinUuid = partnerUuid.toKotlinUuid()
        val directMarriage = (MarriagesTable.husband eq playerKotlinUuid) and
            (MarriagesTable.wife eq partnerKotlinUuid)
        val reverseMarriage = (MarriagesTable.husband eq partnerKotlinUuid) and
            (MarriagesTable.wife eq playerKotlinUuid)

        return transaction(readOnly = true) {
            MarriagesTable
                .select(MarriagesTable.lastInteractionAt)
                .where(directMarriage or reverseMarriage)
                .limit(1)
                .singleOrNull()
                ?.get(MarriagesTable.lastInteractionAt)
        }
    }

    private fun clearStaleMarriagePartner(player: Player, expectedPartnerUuid: UUID) {
        BloodRP.scheduler.runAtEntity(player, null) {
            if (player.marriagePartnerUuid() == expectedPartnerUuid) {
                player.clearMarriagePartner()
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun sendInteractionWarningIfNeeded(player: Player, partnerUuid: UUID, lastInteractionAt: Instant) {
        val warningDays = BloodRP.config.warningDaysWithoutInteraction
        val maxDays = BloodRP.config.maxDaysWithoutInteraction
        if (warningDays <= 0 || maxDays <= 0 || warningDays >= maxDays) {
            return
        }

        val inactivity = Clock.System.now() - lastInteractionAt
        if (inactivity < warningDays.days || inactivity >= maxDays.days) {
            return
        }

        val remainingHours = remainingHours(maxDays.days - inactivity)
        BloodRP.scheduler.runAtEntity(player, null) {
            if (player.marriagePartnerUuid() == partnerUuid) {
                player.sendMessage("Вам нужно поцеловаться, иначе через $remainingHours ч. распадется ваш брак")
            }
        }
    }

    private fun remainingHours(remaining: Duration): Long {
        val remainingMinutes = remaining.inWholeMinutes
        if (remainingMinutes <= 0) {
            return 1
        }

        return (remainingMinutes + MINUTES_IN_HOUR - 1) / MINUTES_IN_HOUR
    }

    private const val MINUTES_IN_HOUR = 60
}
