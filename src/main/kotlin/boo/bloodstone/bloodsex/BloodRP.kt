package boo.bloodstone.bloodsex

import boo.bloodstone.bloodsex.animations.BlowjobAnimation
import boo.bloodstone.bloodsex.animations.DoggyAnimation
import boo.bloodstone.bloodsex.animations.MarryAnimation
import boo.bloodstone.bloodsex.commands.BloodRPCommandRegistrar
import boo.bloodstone.commonBloodLib.Scheduler
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.plugin.java.JavaPlugin
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class BloodRP : JavaPlugin() {
    override fun onEnable() {
        plugin = this

        val requestManager = RequestManager()
        scheduler = Scheduler(this)
        registerActions()
        registerCommands(requestManager)

        installBetterModelResources()
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    private fun registerActions() {
        ActionMaster.register("bj", BlowjobAnimation())
        ActionMaster.register("doggy", DoggyAnimation())
        ActionMaster.register("marry", MarryAnimation())
    }

    private fun registerCommands(requestManager: RequestManager) {
        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
            BloodRPCommandRegistrar.register(event.registrar(), requestManager)
        }
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
        var plugin: BloodRP? = null
        lateinit var scheduler: Scheduler

        private val betterModelModels = listOf(
            "bloodsex_doggy_active",
            "bloodsex_doggy_passive",
        )
    }
}
