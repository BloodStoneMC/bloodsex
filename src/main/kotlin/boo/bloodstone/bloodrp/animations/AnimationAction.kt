package boo.bloodstone.bloodrp.animations

import boo.bloodstone.bloodrp.Action
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player

abstract class AnimationAction(private val request: String) : Action {
    override fun notify(firstPlayer: Player, secondPlayer: Player) {
        firstPlayer.sendMessage("Вы отправили предложение ${secondPlayer.name}")
        secondPlayer.sendMessage("${firstPlayer.name} $request")
        secondPlayer.sendMessage(
            Component.text("[Принять]")
                .color(NamedTextColor.GREEN)
                .clickEvent(
                    ClickEvent.clickEvent(
                        ClickEvent.Action.RUN_COMMAND,
                        "/bloodrp accept ${firstPlayer.name}"
                    )
                )
        )
    }
}
