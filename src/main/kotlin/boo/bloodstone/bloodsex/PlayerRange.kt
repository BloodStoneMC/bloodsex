package boo.bloodstone.bloodsex

import org.bukkit.entity.Player

private const val MAX_ACTION_DISTANCE = 16.0
const val MAX_RAPE_DISTANCE = 5.0

fun Player.isCloseEnoughTo(partner: Player, maxDistance: Double = MAX_ACTION_DISTANCE): Boolean {
    return location.world == partner.location.world && location.distance(partner.location) <= maxDistance
}
