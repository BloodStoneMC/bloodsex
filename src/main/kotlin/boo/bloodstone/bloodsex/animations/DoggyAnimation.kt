package boo.bloodstone.bloodsex.animations

import boo.bloodstone.bloodsex.BloodRP
import kr.toxicity.model.api.BetterModel
import kr.toxicity.model.api.animation.AnimationModifier
import kr.toxicity.model.api.bukkit.platform.BukkitAdapter
import kr.toxicity.model.api.tracker.Tracker
import kr.toxicity.model.api.tracker.TrackerModifier
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import kotlin.jvm.optionals.getOrNull

class DoggyAnimation : AnimationAction("предложил вам догги-стайл") {
    override fun play(firstPlayer: Player, secondPlayer: Player) {
        val session = start(firstPlayer, secondPlayer)

        BloodRP.scheduler.runInRegionLater(firstPlayer.location, 30 * 20) {
            stop(firstPlayer, secondPlayer, session)
        }
    }

    private fun start(firstPlayer: Player, secondPlayer: Player): DoggySession {
        val direction: Vector = secondPlayer.location.subtract(firstPlayer.location).toVector().normalize()
        val newPosition: Vector = firstPlayer.location.add(direction.multiply(0.85)).toVector()
        secondPlayer.teleportAsync(newPosition.toLocation(secondPlayer.world))
        secondPlayer.location.pitch = 0.0f
        secondPlayer.location.setDirection(direction)
        secondPlayer.location.pitch = 0.0f
        firstPlayer.location.setDirection(direction)
        secondPlayer.teleportAsync(secondPlayer.location)
        secondPlayer.teleportAsync(secondPlayer.location.setDirection(direction))

        val initialLocation: Location = firstPlayer.location.clone()
        for (x in 0..14) {
            val maxOffset = 0.3
            val offsetX = (Math.random() - 0.3) * 2 * maxOffset
            val offsetY = (Math.random() - 0.3) * 2 * maxOffset
            val offsetZ = (Math.random() - 0.3) * 2 * maxOffset

            val particleLocation = initialLocation.clone().add(offsetX, offsetY, offsetZ)
            particleLocation.world.spawnParticle(Particle.HEART, particleLocation, 1, 0.0, 0.0, 0.0, 0.0)
        }

        var activeTracker: Tracker? = null
        var passiveTracker: Tracker? = null

        if (firstPlayer.isOnline) {
            activeTracker = playBetterModel(firstPlayer, "bloodsex_doggy_active")
        }
        if (secondPlayer.isOnline) {
            passiveTracker = playBetterModel(secondPlayer, "bloodsex_doggy_passive")
        }

        return DoggySession(activeTracker, passiveTracker)
    }

    private fun stop(firstPlayer: Player, secondPlayer: Player, session: DoggySession) {
        session.close()

        val initialLocation: Location = firstPlayer.location.clone()
        val particleDirection: Vector = secondPlayer.location.subtract(initialLocation).toVector().normalize()

        for (x in 0..14) {
            val spawnLocation = initialLocation.clone().add(particleDirection.multiply(x))
            firstPlayer.world.spawnParticle(Particle.CLOUD, spawnLocation, 1)
        }

        if (firstPlayer.isOnline) {
            if (firstPlayer.isSneaking) firstPlayer.isSneaking = false
        }

        if (secondPlayer.isOnline) {
            if (secondPlayer.isSneaking) secondPlayer.isSneaking = false
        }
    }

    private fun playBetterModel(player: Player, modelName: String): Tracker? {
        val renderer = BetterModel.model(modelName).getOrNull() ?: run {
            player.sendMessage("Модель BetterModel $modelName не найдена. Перезагрузи BetterModel.")
            return null
        }

        val tracker = renderer.create(
            BukkitAdapter.adapt(player),
            TrackerModifier.builder()
                .damageAnimation(false)
                .damageTint(false)
                .build()
        )

        if (!tracker.animate("doggy", AnimationModifier.DEFAULT)) {
            tracker.close()
            player.sendMessage("Анимация BetterModel doggy не найдена в $modelName.")
            return null
        }

        return tracker
    }
}

private data class DoggySession(
    private val activeTracker: Tracker?,
    private val passiveTracker: Tracker?,
) {
    fun close() {
        activeTracker?.close()
        passiveTracker?.close()
    }
}
