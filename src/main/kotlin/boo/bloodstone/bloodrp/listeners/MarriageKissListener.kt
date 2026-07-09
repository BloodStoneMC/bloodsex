package boo.bloodstone.bloodrp.listeners

import boo.bloodstone.bloodrp.ActionMaster
import boo.bloodstone.bloodrp.marriagePartnerUuid
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerToggleSneakEvent

object MarriageKissListener : Listener {
    private const val KISS_ACTION_NAME = "kiss"
    private const val KISS_DISTANCE_SQUARED = 4.0

    @EventHandler(ignoreCancelled = true)
    fun onPlayerToggleSneak(event: PlayerToggleSneakEvent) {
        if (!event.isSneaking) {
            return
        }

        val player = event.player
        val partnerUuid = player.marriagePartnerUuid() ?: return
        val partner = Bukkit.getPlayer(partnerUuid) ?: return

        if (!player.isCloseEnoughForKiss(partner) || partner.marriagePartnerUuid() != player.uniqueId) {
            return
        }

        ActionMaster.fromName(KISS_ACTION_NAME)?.play(player, partner)
    }

    private fun Player.isCloseEnoughForKiss(partner: Player): Boolean =
        world == partner.world && location.distanceSquared(partner.location) <= KISS_DISTANCE_SQUARED
}
