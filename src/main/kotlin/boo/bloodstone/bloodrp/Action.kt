package boo.bloodstone.bloodrp

import org.bukkit.entity.Player

interface Action {
    fun notify(firstPlayer: Player, secondPlayer: Player, request: ActionRequest)
    fun play(firstPlayer: Player, secondPlayer: Player)
}
