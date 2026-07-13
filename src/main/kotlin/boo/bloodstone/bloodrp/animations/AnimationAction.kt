package boo.bloodstone.bloodrp.animations

import boo.bloodstone.bloodrp.Action
import boo.bloodstone.bloodrp.ActionRequest
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player

abstract class AnimationAction(private val requestText: String) : Action {
    override fun notify(firstPlayer: Player, secondPlayer: Player, actionRequest: ActionRequest) {
        firstPlayer.sendMessage("Вы отправили предложение ${secondPlayer.name}")
        secondPlayer.sendMessage("${firstPlayer.name} $requestText")
        secondPlayer.sendMessage(
            Component.text("[Принять]")
                .color(NamedTextColor.GREEN)
                .clickEvent(
                    ClickEvent.clickEvent(
                        ClickEvent.Action.RUN_COMMAND,
                        "/bloodrp accept ${actionRequest.id}"
                    )
                )
        )
    }
}
