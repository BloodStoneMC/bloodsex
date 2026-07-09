package boo.bloodstone.bloodsex.animations

import boo.bloodstone.bloodsex.BloodRP
import boo.bloodstone.bloodsex.database.MarriagesTable
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toKotlinUuid

class KissAnimation : AnimationAction("предложил вам поцелуй") {
    override fun play(firstPlayer: Player, secondPlayer: Player) {
        updateMarriageInteraction(firstPlayer, secondPlayer)

        val task = BloodRP.scheduler.runInRegionAtFixedRate(firstPlayer.location, 1, 10) {
            if (firstPlayer.isOnline && secondPlayer.isOnline && firstPlayer.world == secondPlayer.world) {
                spawnHearts(firstPlayer, secondPlayer)
            }
        }

        BloodRP.scheduler.runInRegionLater(firstPlayer.location, 4 * 20) {
            task.cancel()
        }
    }

    private fun spawnHearts(firstPlayer: Player, secondPlayer: Player) {
        val firstLocation = firstPlayer.location.clone().add(0.0, 1.4, 0.0)
        val secondLocation = secondPlayer.location.clone().add(0.0, 1.4, 0.0)
        val direction = secondLocation.toVector().subtract(firstLocation.toVector())
        val midpoint = firstLocation.clone().add(direction.multiply(0.5))

        firstPlayer.world.spawnParticle(Particle.HEART, midpoint, 6, 0.35, 0.25, 0.35, 0.02)
    }

    @OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)
    private fun updateMarriageInteraction(firstPlayer: Player, secondPlayer: Player) {
        val firstUuid = firstPlayer.uniqueId.toKotlinUuid()
        val secondUuid = secondPlayer.uniqueId.toKotlinUuid()
        val directMarriage = (MarriagesTable.husband eq firstUuid) and (MarriagesTable.wife eq secondUuid)
        val reverseMarriage = (MarriagesTable.husband eq secondUuid) and (MarriagesTable.wife eq firstUuid)
        val now = Clock.System.now()

        transaction {
            MarriagesTable.update({ directMarriage or reverseMarriage }) {
                it[MarriagesTable.lastInteractionAt] = now
            }
        }
    }
}
