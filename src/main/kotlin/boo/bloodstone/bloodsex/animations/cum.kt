package boo.bloodstone.bloodsex.animations

import boo.bloodstone.commonBloodLib.Scheduler
import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.util.Vector

fun cum(scheduler: Scheduler, location: Location) {
    location.y += 1.5
    val task = scheduler.runInRegionAtFixedRate(location,1,10) {
        val entity = location.world.spawnEntity(location, EntityType.LLAMA_SPIT)

        entity.velocity = location.direction.multiply(0.2)
    }

    scheduler.runInRegionLater(location, 31) {
        task.cancel()
    }

}