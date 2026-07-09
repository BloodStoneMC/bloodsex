package boo.bloodstone.bloodsex.animations

import boo.bloodstone.bloodsex.BloodRP
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Firework
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class MarryAnimation : AnimationAction("сделал вам предложение") {
    override fun play(firstPlayer: Player, secondPlayer: Player) {
        val firework = firstPlayer.world.spawnEntity(
            firstPlayer.location,
            EntityType.FIREWORK_ROCKET
        ) as Firework

        val fireworkMeta = firework.fireworkMeta.clone()

        fireworkMeta.power = 2
        fireworkMeta.addEffect(FireworkEffect.builder().withColor(Color.RED).flicker(true).build());

        firework.fireworkMeta = fireworkMeta

        for (i in 1..40) {
            BloodRP.scheduler.runInRegionLater(firstPlayer.location, (i * 5).toLong()) {
                val firework = firstPlayer.world.spawnEntity(
                    firstPlayer.location,
                    EntityType.FIREWORK_ROCKET
                ) as Firework
                firework.fireworkMeta = fireworkMeta

                val firework2 = firstPlayer.world.spawnEntity(
                    secondPlayer.location,
                    EntityType.FIREWORK_ROCKET
                ) as Firework
                firework2.fireworkMeta = fireworkMeta
            }
        }

        val cake = ItemStack(Material.CAKE)

        val cakeMeta = cake.itemMeta

        cakeMeta.displayName(
            Component.text("Свадебный торт ${firstPlayer.name} и ${secondPlayer.name}").color(NamedTextColor.AQUA)
                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
        )
        cake.itemMeta = cakeMeta

        firstPlayer.inventory.addItem(ItemStack(Material.CAKE))

        for (player in Bukkit.getOnlinePlayers()) {
            player.sendMessage("Теперь ${firstPlayer.name} и ${secondPlayer.name} женаты!!!")
            player.sendMessage("Поздравим их")
        }
    }
}
