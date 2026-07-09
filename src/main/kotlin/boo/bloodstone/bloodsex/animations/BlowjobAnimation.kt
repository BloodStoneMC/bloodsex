package boo.bloodstone.bloodsex.animations

import boo.bloodstone.bloodsex.BloodRP
import dev.geco.gsit.api.GSitAPI
import org.bukkit.entity.Player

class BlowjobAnimation : AnimationAction("предложил сделать вам минет") {
    private val cumAnimation = CumAnimation()

    override fun play(firstPlayer: Player, secondPlayer: Player) {
        val movingPlayer = firstPlayer
        val sittingPlayer = secondPlayer

        if (!GSitAPI.isEntitySitting(sittingPlayer)) {
            sittingPlayer.sendMessage("Присядь сначала")
            movingPlayer.sendMessage("Ваш партнер не сидит")
            return
        }

        val yaw = sittingPlayer.yaw;
        var newYaw: Float;

        if (yaw > 0) {
            newYaw = yaw - 180f
        } else {
            newYaw = yaw + 180f
        }

        val newLocation = sittingPlayer.location.clone()

        if (yaw in -45.0..45.0) {
            newLocation.z += 0.8
        }
        if (yaw in 45.0..135.0) {
            newLocation.x -= 0.8
        }
        if (yaw in 135.0..180.0 || yaw in -180.0..-135.0) {
            newLocation.z -= 0.8
        }
        if (yaw in -135.0..-45.0) {
            newLocation.x += 0.8
        }

        newLocation.y -= 0.5
        newLocation.yaw = newYaw
        newLocation.pitch = 50.0F
        movingPlayer.teleportAsync(newLocation)

        var tickCounter = 0;
        var isPitchLowering = true;
        val scheduler = BloodRP.scheduler

        val task = scheduler.runInRegionAtFixedRate(sittingPlayer.location, 1, 1) {
            if (isPitchLowering) {
                newLocation.pitch -= 2.0f
            } else {
                newLocation.pitch += 2.0f
            }

            if (newLocation.pitch < 0 || newLocation.pitch > 50) {
                isPitchLowering = !isPitchLowering
            }

            movingPlayer.teleportAsync(newLocation)
            tickCounter++;
        }

        scheduler.runInRegionLater(sittingPlayer.location, 30 * 20) {
            cumAnimation.play(sittingPlayer.location.clone())
            task.cancel()
        }
    }
}
