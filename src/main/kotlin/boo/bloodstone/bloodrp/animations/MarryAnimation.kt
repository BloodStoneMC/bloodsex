package boo.bloodstone.bloodrp.animations

import boo.bloodstone.bloodrp.BloodRP
import boo.bloodstone.bloodrp.database.MarriageRepository
import boo.bloodstone.bloodrp.setMarriagePartner
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
import java.util.UUID
import java.util.logging.Level
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class MarryAnimation : AnimationAction("сделал вам предложение") {
    @OptIn(ExperimentalTime::class)
    override fun play(firstPlayer: Player, secondPlayer: Player) {
        val marriedPlayerIds = try {
            MarriageRepository.createMarriage(firstPlayer.uniqueId, secondPlayer.uniqueId, Clock.System.now())
        } catch (exception: Exception) {
            BloodRP.plugin.logger.log(Level.WARNING, "Failed to save marriage.", exception)
            sendToBoth(firstPlayer, secondPlayer, "Не удалось зарегистрировать брак")
            return
        }

        if (marriedPlayerIds.isNotEmpty()) {
            notifyAlreadyMarried(firstPlayer, secondPlayer, marriedPlayerIds)
            return
        }

        firstPlayer.setMarriagePartner(secondPlayer)
        secondPlayer.setMarriagePartner(firstPlayer)
        giveWeddingCake(firstPlayer, secondPlayer)
        playFireworks(firstPlayer, secondPlayer)

        Bukkit.getOnlinePlayers().forEach { player ->
            player.sendMessage("Теперь ${firstPlayer.name} и ${secondPlayer.name} женаты!!!")
            player.sendMessage("Поздравим их")
        }
    }

    private fun giveWeddingCake(firstPlayer: Player, secondPlayer: Player) {
        val cake = ItemStack(Material.CAKE)
        val meta = cake.itemMeta
        meta.displayName(
            Component.text("Свадебный торт ${firstPlayer.name} и ${secondPlayer.name}")
                .color(NamedTextColor.AQUA)
                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
        )
        cake.itemMeta = meta
        firstPlayer.inventory.addItem(cake).values.forEach { item ->
            firstPlayer.world.dropItemNaturally(firstPlayer.location, item)
        }
    }

    private fun playFireworks(firstPlayer: Player, secondPlayer: Player) {
        val effect = FireworkEffect.builder().withColor(Color.RED).flicker(true).build()
        spawnFirework(firstPlayer, effect)
        repeat(40) { index ->
            BloodRP.scheduler.runInRegionLater(firstPlayer.location, ((index + 1) * 5).toLong()) {
                spawnFirework(firstPlayer, effect)
                spawnFirework(secondPlayer, effect)
            }
        }
    }

    private fun spawnFirework(player: Player, effect: FireworkEffect) {
        val firework = player.world.spawnEntity(player.location, EntityType.FIREWORK_ROCKET) as Firework
        firework.fireworkMeta = firework.fireworkMeta.apply {
            power = 2
            addEffect(effect)
        }
    }

    private fun notifyAlreadyMarried(firstPlayer: Player, secondPlayer: Player, marriedPlayerIds: Set<UUID>) {
        val names = listOf(firstPlayer, secondPlayer)
            .filter { it.uniqueId in marriedPlayerIds }
            .joinToString("\n") { "${it.name} уже в браке" }
        sendToBoth(firstPlayer, secondPlayer, names)
    }

    private fun sendToBoth(firstPlayer: Player, secondPlayer: Player, message: String) {
        firstPlayer.sendMessage(message)
        secondPlayer.sendMessage(message)
    }
}
