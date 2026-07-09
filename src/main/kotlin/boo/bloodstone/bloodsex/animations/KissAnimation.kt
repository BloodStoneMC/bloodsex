package boo.bloodstone.bloodsex.animations

import boo.bloodstone.bloodsex.BloodRP
import org.bukkit.Particle
import org.bukkit.entity.Player

class KissAnimation : AnimationAction("предложил вам поцелуй") {
    override fun play(firstPlayer: Player, secondPlayer: Player) {
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
}
