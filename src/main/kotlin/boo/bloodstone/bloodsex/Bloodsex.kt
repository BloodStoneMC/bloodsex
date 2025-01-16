package boo.bloodstone.bloodsex

import com.github.trard.Scheduler
import dev.kosmx.playerAnim.core.data.KeyframeAnimation
import org.bukkit.Bukkit
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin

class Bloodsex : JavaPlugin() {
    lateinit var activeEmote: List<KeyframeAnimation>
    lateinit var passiveEmote:  List<KeyframeAnimation>

    override fun onEnable() {
        plugin = this

        val requestManager = RequestManager()
        val scheduler = Scheduler(this)

        activeEmote = deserializeEmote("emotes/active.json")!!
        passiveEmote = deserializeEmote("emotes/passive.json")!!

        getCommand("bj")!!.setExecutor(RequestCommand(requestManager))
        getCommand("doggy")!!.setExecutor(RequestCommand(requestManager))
        getCommand("marry")!!.setExecutor(RequestCommand(requestManager))

        getCommand("accept")!!.setExecutor(AcceptCommand(requestManager, scheduler))
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    companion object {
        var plugin: Bloodsex? = null
    }
}