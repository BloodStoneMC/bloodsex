package boo.bloodstone.bloodrp.tasks

import boo.bloodstone.bloodrp.BloodRP
import boo.bloodstone.bloodrp.clearMarriagePartner
import boo.bloodstone.bloodrp.database.MarriagesTable
import boo.bloodstone.bloodrp.marriagePartnerUuid
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.jetbrains.exposed.v1.core.lessEq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.UUID
import java.util.logging.Level
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toJavaUuid

object MarriageInteractionExpirationTask {
    private const val CHECK_PERIOD_MILLIS = 60_000L
    private val expirationMessage = Component.text("Ваш брак распался из-за отсутствия взаимодействия")

    fun start() {
        BloodRP.coroutineScope.launch {
            while (isActive) {
                runCatching(::checkMarriages).onFailure { exception ->
                    BloodRP.plugin.logger.log(Level.WARNING, "Failed to check marriage inactivity.", exception)
                }
                delay(CHECK_PERIOD_MILLIS)
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun checkMarriages() {
        val maxDays = BloodRP.config.maxDaysWithoutInteraction
        if (maxDays <= 0) return

        val marriages = expireMarriages(Clock.System.now() - maxDays.days)
        if (marriages.isEmpty()) return

        BloodRP.scheduler.runGlobal {
            marriages.forEach { (husbandId, wifeId) ->
                notifyOnlineSpouse(Bukkit.getPlayer(husbandId), wifeId)
                notifyOnlineSpouse(Bukkit.getPlayer(wifeId), husbandId)
            }
        }
    }

    @OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)
    private fun expireMarriages(expirationThreshold: Instant): List<Pair<UUID, UUID>> = transaction {
        val marriages = MarriagesTable
            .select(MarriagesTable.husband, MarriagesTable.wife)
            .where(MarriagesTable.lastInteractionAt lessEq expirationThreshold)
            .map { row -> row[MarriagesTable.husband].toJavaUuid() to row[MarriagesTable.wife].toJavaUuid() }

        if (marriages.isNotEmpty()) {
            MarriagesTable.deleteWhere { MarriagesTable.lastInteractionAt lessEq expirationThreshold }
        }
        marriages
    }

    private fun notifyOnlineSpouse(player: Player?, expectedPartnerId: UUID) {
        player ?: return
        BloodRP.scheduler.runAtEntity(player, null) {
            player.clearMarriagePartnerIfMatches(expectedPartnerId)
            player.sendMessage(expirationMessage)
        }
    }

    private fun Player.clearMarriagePartnerIfMatches(expectedPartnerId: UUID) {
        if (marriagePartnerUuid() == expectedPartnerId) clearMarriagePartner()
    }
}
