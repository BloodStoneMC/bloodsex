package boo.bloodstone.bloodsex

import dev.kosmx.playerAnim.core.data.KeyframeAnimation
import io.github.kosmx.emotes.api.events.server.ServerEmoteAPI
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import java.util.logging.Level

fun deserializeEmote(path: String): List<KeyframeAnimation>? {
    val stream = Bloodsex.plugin!!.getResource(path)
    if (stream == null) {
        Bukkit.getServer().logger.log(Level.SEVERE, "Не удалось загрузить эмоцию: $path")
        return null
    }

    return ServerEmoteAPI.deserializeEmote(stream, path, "json")
}