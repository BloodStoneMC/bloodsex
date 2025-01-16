package boo.bloodstone.bloodsex.animations

import boo.bloodstone.bloodsex.Bloodsex
import com.github.trard.Scheduler
import io.github.kosmx.emotes.api.events.server.ServerEmoteAPI
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.util.Vector

fun doggy(firstPlayer: Player, secondPlayer: Player, scheduler: Scheduler) {
    start(secondPlayer, firstPlayer)

    scheduler.runInRegionLater(secondPlayer.location, 30 * 20) {
        stop(secondPlayer, firstPlayer)
    }
}

private fun start(firstPlayer: Player, secondPlayer: Player) {
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

    if (firstPlayer.isOnline) {
        ServerEmoteAPI.forcePlayEmote(firstPlayer.uniqueId, Bloodsex.plugin!!.activeEmote[0])
    }
    if (secondPlayer.isOnline) {
        ServerEmoteAPI.forcePlayEmote(secondPlayer.uniqueId, Bloodsex.plugin!!.passiveEmote[0])
    }
}

private fun stop(firstPlayer: Player, secondPlayer: Player) {
    val initialLocation: Location = firstPlayer.location.clone()
    val particleDirection: Vector = secondPlayer.location.subtract(initialLocation).toVector().normalize()

    for (x in 0..14) {
        val spawnLocation = initialLocation.clone().add(particleDirection.multiply(x))
        firstPlayer.world.spawnParticle(Particle.CLOUD, spawnLocation, 1)
    }

    if (firstPlayer.isOnline) {
        ServerEmoteAPI.setPlayerPlayingEmote(firstPlayer.uniqueId, null)
        if (firstPlayer.isSneaking) firstPlayer.isSneaking = false
    }

    if (secondPlayer.isOnline) {
        ServerEmoteAPI.setPlayerPlayingEmote(secondPlayer.uniqueId, null)
        if (secondPlayer.isSneaking) secondPlayer.isSneaking = false
    }
}