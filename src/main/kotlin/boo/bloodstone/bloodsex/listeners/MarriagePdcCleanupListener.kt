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
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toKotlinUuid

object MarriagePdcCleanupListener : Listener {
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        val playerUuid = player.uniqueId
        val partnerUuid = player.marriagePartnerUuid() ?: return

        BloodRP.coroutineScope.launch {
            val hasMarriage = try {
                hasMarriage(playerUuid, partnerUuid)
            } catch (exception: Exception) {
                BloodRP.plugin.logger.log(Level.WARNING, "Failed to verify marriage PDC for $playerUuid.", exception)
                return@launch
            }

            if (!hasMarriage) {
                clearStaleMarriagePartner(player, partnerUuid)
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun hasMarriage(playerUuid: UUID, partnerUuid: UUID): Boolean {
        val playerKotlinUuid = playerUuid.toKotlinUuid()
        val partnerKotlinUuid = partnerUuid.toKotlinUuid()
        val directMarriage = (MarriagesTable.husband eq playerKotlinUuid) and
            (MarriagesTable.wife eq partnerKotlinUuid)
        val reverseMarriage = (MarriagesTable.husband eq partnerKotlinUuid) and
            (MarriagesTable.wife eq playerKotlinUuid)

        return transaction(readOnly = true) {
            MarriagesTable
                .select(MarriagesTable.id)
                .where(directMarriage or reverseMarriage)
                .limit(1)
                .singleOrNull() != null
        }
    }

    private fun clearStaleMarriagePartner(player: Player, expectedPartnerUuid: UUID) {
        BloodRP.scheduler.runAtEntity(player, null) {
            if (player.marriagePartnerUuid() == expectedPartnerUuid) {
                player.clearMarriagePartner()
            }
        }
    }
}
