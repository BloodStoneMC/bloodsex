package boo.bloodstone.bloodrp.listeners

import boo.bloodstone.bloodrp.BloodRP
import boo.bloodstone.bloodrp.clearMarriagePartner
import boo.bloodstone.bloodrp.database.MarriageRepository
import boo.bloodstone.bloodrp.database.PlayerMarriage
import boo.bloodstone.bloodrp.marriagePartnerNeedsUpdate
import boo.bloodstone.bloodrp.marriagePartnerValue
import boo.bloodstone.bloodrp.setMarriagePartner
import kotlinx.coroutines.launch
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import java.util.logging.Level
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime

object MarriagePdcListener : Listener {
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        val initialPartnerValue = player.marriagePartnerValue()

        BloodRP.coroutineScope.launch {
            val marriage = runCatching { MarriageRepository.findMarriage(player.uniqueId) }
                .onFailure { exception ->
                    BloodRP.plugin.logger.log(
                        Level.WARNING,
                        "Failed to reconcile marriage PDC for ${player.uniqueId}.",
                        exception
                    )
                }
                .getOrElse { return@launch }

            updatePlayer(player, initialPartnerValue, marriage)
        }
    }

    private fun updatePlayer(player: Player, initialPartnerValue: String?, marriage: PlayerMarriage?) {
        BloodRP.scheduler.runAtEntity(player, null) {
            if (player.marriagePartnerValue() != initialPartnerValue) return@runAtEntity

            val databasePartnerId = marriage?.partnerId
            if (marriagePartnerNeedsUpdate(player.marriagePartnerValue(), databasePartnerId)) {
                if (databasePartnerId == null) player.clearMarriagePartner() else player.setMarriagePartner(databasePartnerId)
            }

            interactionWarning(marriage)?.let { player.sendMessage(it) }
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun interactionWarning(marriage: PlayerMarriage?): String? {
        marriage ?: return null

        val warningDays = BloodRP.config.warningDaysWithoutInteraction
        val maxDays = BloodRP.config.maxDaysWithoutInteraction
        if (warningDays <= 0 || maxDays <= warningDays) return null

        val inactivity = Clock.System.now() - marriage.lastInteractionAt
        if (inactivity < warningDays.days || inactivity >= maxDays.days) return null

        return "Вам нужно поцеловаться, иначе через ${remainingHours(maxDays.days - inactivity)} ч. распадется ваш брак"
    }

    private fun remainingHours(remaining: Duration): Long = maxOf(1, (remaining.inWholeMinutes + 59) / 60)
}
