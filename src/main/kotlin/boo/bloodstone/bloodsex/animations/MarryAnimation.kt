package boo.bloodstone.bloodsex.animations

import boo.bloodstone.bloodsex.BloodRP
import boo.bloodstone.bloodsex.database.MarriagesTable
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.EntityType
import org.bukkit.entity.Firework
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toKotlinUuid

class MarryAnimation : AnimationAction("сделал вам предложение") {
    override fun play(firstPlayer: Player, secondPlayer: Player) {
        if (!notifyIfAlreadyMarried(firstPlayer, secondPlayer)) {
            return
        }

        val marriageId = saveMarriage(firstPlayer, secondPlayer)
        setMarriageId(firstPlayer, marriageId)
        setMarriageId(secondPlayer, marriageId)

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

    @OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)
    private fun saveMarriage(husband: Player, wife: Player): Int {
        val now = Clock.System.now()

        return transaction {
            MarriagesTable.insertAndGetId {
                it[MarriagesTable.husband] = husband.uniqueId.toKotlinUuid()
                it[MarriagesTable.wife] = wife.uniqueId.toKotlinUuid()
                it[MarriagesTable.startedAt] = now
                it[MarriagesTable.lastInteractionAt] = now
            }.value
        }
    }

    private fun setMarriageId(player: Player, marriageId: Int) {
        player.persistentDataContainer.set(marriageIdKey, PersistentDataType.INTEGER, marriageId)
    }

    private fun notifyIfAlreadyMarried(firstPlayer: Player, secondPlayer: Player): Boolean {
        val marriedPlayers = getMarriedPlayers(firstPlayer, secondPlayer)

        if (marriedPlayers.isEmpty()) {
            return true
        }

        val message = marriedPlayers.joinToString("\n") { "${it.name} уже в браке" }
        firstPlayer.sendMessage(message)
        secondPlayer.sendMessage(message)
        return false
    }

    companion object {
        private val marriageIdKey = NamespacedKey("bloodrp", "marriage_id")

        @OptIn(ExperimentalUuidApi::class)
        fun getMarriedPlayers(firstPlayer: Player, secondPlayer: Player): List<Player> {
            return listOf(firstPlayer, secondPlayer).filter { isMarried(it.uniqueId.toKotlinUuid()) }
        }

        @OptIn(ExperimentalUuidApi::class)
        private fun isMarried(player: Uuid): Boolean = transaction(readOnly = true) {
            MarriagesTable
                .select(MarriagesTable.id)
                .where((MarriagesTable.husband eq player) or (MarriagesTable.wife eq player))
                .limit(1)
                .singleOrNull() != null
        }
    }
}
