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

    fun start() {
        BloodRP.coroutineScope.launch {
            while (isActive) {
                try {
                    checkMarriages()
                } catch (exception: Exception) {
                    BloodRP.plugin.logger.log(Level.WARNING, "Failed to check marriage inactivity.", exception)
                }

                delay(CHECK_PERIOD_MILLIS)
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun checkMarriages() {
        val maxDaysWithoutInteraction = BloodRP.config.maxDaysWithoutInteraction
        if (maxDaysWithoutInteraction <= 0) {
            return
        }

        val expirationThreshold = Clock.System.now() - maxDaysWithoutInteraction.days
        val marriages = expireMarriages(expirationThreshold)
        if (marriages.isEmpty()) {
            return
        }

        notifyOnlineSpouses(
            marriages,
            Component.text("Ваш брак распался из-за отсутствия взаимодействия")
        )
    }

    @OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)
    private fun expireMarriages(expirationThreshold: Instant): List<MarriageInteractionEntry> =
        transaction {
            val marriages = MarriagesTable
                .select(MarriagesTable.husband, MarriagesTable.wife)
                .where(MarriagesTable.lastInteractionAt lessEq expirationThreshold)
                .map { row ->
                    MarriageInteractionEntry(
                        husband = row[MarriagesTable.husband].toJavaUuid(),
                        wife = row[MarriagesTable.wife].toJavaUuid(),
                    )
                }

            if (marriages.isNotEmpty()) {
                MarriagesTable.deleteWhere {
                    MarriagesTable.lastInteractionAt lessEq expirationThreshold
                }
            }

            marriages
        }

    private fun notifyOnlineSpouses(
        marriages: List<MarriageInteractionEntry>,
        message: Component,
    ) {
        BloodRP.scheduler.runGlobal {
            val notifications = linkedMapOf<UUID, MarriagePlayerNotification>()
            marriages.forEach { marriage ->
                Bukkit.getPlayer(marriage.husband)?.let { player ->
                    notifications.putIfAbsent(
                        player.uniqueId,
                        MarriagePlayerNotification(player, marriage.wife)
                    )
                }
                Bukkit.getPlayer(marriage.wife)?.let { player ->
                    notifications.putIfAbsent(
                        player.uniqueId,
                        MarriagePlayerNotification(player, marriage.husband)
                    )
                }
            }

            notifications.values.forEach { notification ->
                notifyOnlineSpouse(notification, message)
            }
        }
    }

    private fun notifyOnlineSpouse(notification: MarriagePlayerNotification, message: Component) {
        BloodRP.scheduler.runAtEntity(notification.player, null) {
            notification.player.clearMarriagePartnerIfMatches(notification.expectedPartnerUuid)
            notification.player.sendMessage(message)
        }
    }

    private fun Player.clearMarriagePartnerIfMatches(expectedPartnerUuid: UUID) {
        if (marriagePartnerUuid() == expectedPartnerUuid) {
            clearMarriagePartner()
        }
    }

    private data class MarriageInteractionEntry(
        val husband: UUID,
        val wife: UUID,
    )

    private data class MarriagePlayerNotification(
        val player: Player,
        val expectedPartnerUuid: UUID,
    )
}
