package boo.bloodstone.bloodrp.listeners

import boo.bloodstone.bloodrp.ActionMaster
import boo.bloodstone.bloodrp.BloodRP
import boo.bloodstone.bloodrp.MarriageKissCooldown
import boo.bloodstone.bloodrp.marriagePartnerUuid
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerToggleSneakEvent
import java.time.Duration

object MarriageKissListener : Listener {
    private const val KISS_DISTANCE_SQUARED = 4.0
    private val cooldown = MarriageKissCooldown()

    @EventHandler(ignoreCancelled = true)
    fun onPlayerToggleSneak(event: PlayerToggleSneakEvent) {
        if (!event.isSneaking) return

        val player = event.player
        val partner = player.marriagePartnerUuid()?.let { Bukkit.getPlayer(it) } ?: return
        if (!player.canKiss(partner)) return

        val duration = Duration.ofSeconds(BloodRP.config.sneakKissCooldownSeconds)
        if (cooldown.tryAcquire(player.uniqueId, partner.uniqueId, duration)) {
            ActionMaster.fromName("kiss")?.play(player, partner)
        }
    }

    private fun Player.canKiss(partner: Player): Boolean =
        partner.marriagePartnerUuid() == uniqueId && world == partner.world &&
            location.distanceSquared(partner.location) <= KISS_DISTANCE_SQUARED
}
