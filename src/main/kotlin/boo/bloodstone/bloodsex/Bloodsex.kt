package boo.bloodstone.bloodsex

import boo.bloodstone.commonBloodLib.Scheduler
import org.bukkit.plugin.java.JavaPlugin
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class Bloodsex : JavaPlugin() {
    override fun onEnable() {
        plugin = this

        val requestManager = RequestManager()
        val scheduler = Scheduler(this)

        installBetterModelResources()

        getCommand("bj")!!.setExecutor(RequestCommand(requestManager))
        getCommand("doggy")!!.setExecutor(RequestCommand(requestManager))
        getCommand("sex")!!.setExecutor(RequestCommand(requestManager))
        getCommand("marry")!!.setExecutor(RequestCommand(requestManager))
        val directActionCommand = DirectActionCommand(scheduler)
        getCommand("rape")!!.setExecutor(directActionCommand)
        getCommand("rape")!!.tabCompleter = directActionCommand

        getCommand("accept")!!.setExecutor(AcceptCommand(requestManager, scheduler))
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    private fun installBetterModelResources() {
        val pluginsDirectory = dataFolder.toPath().parent ?: dataFolder.toPath()
        val destination = pluginsDirectory.resolve("BetterModel/models/bloodsex")
        Files.createDirectories(destination)

        for (model in betterModelModels) {
            val resourcePath = "bettermodel/$model.bbmodel"
            val targetPath = destination.resolve("$model.bbmodel")
            getResource(resourcePath).use { input ->
                if (input == null) {
                    logger.warning("BetterModel resource not found: $resourcePath")
                    return@use
                }
                Files.copy(input, targetPath, StandardCopyOption.REPLACE_EXISTING)
            }
        }
    }

    companion object {
        var plugin: Bloodsex? = null

        private val betterModelModels = listOf(
            "bloodsex_doggy_active",
            "bloodsex_doggy_passive",
        )
    }
}
