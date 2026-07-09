package boo.bloodstone.bloodsex

import org.bukkit.entity.Player

data class ActionRequest(
    val requester: Player,
    val action: Action,
)
