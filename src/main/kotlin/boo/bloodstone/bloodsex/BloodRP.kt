package boo.bloodstone.bloodsex

import boo.bloodstone.bloodsex.animations.BlowjobAnimation
import boo.bloodstone.bloodsex.animations.DoggyAnimation
import boo.bloodstone.bloodsex.animations.KissAnimation
import boo.bloodstone.bloodsex.animations.MarryAnimation
import boo.bloodstone.bloodsex.commands.BloodRPCommandRegistrar
import boo.bloodstone.bloodsex.config.BloodRPConfig
import boo.bloodstone.bloodsex.config.BloodRPConfigLoader
import boo.bloodstone.bloodsex.database.MarriagesTable
import boo.bloodstone.bloodsex.listeners.MarriageKissListener
import boo.bloodstone.bloodsex.listeners.MarriagePdcCleanupListener
import boo.bloodstone.bloodsex.tasks.MarriageInteractionExpirationTask
import boo.bloodstone.commonBloodLib.Scheduler
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class BloodRP : JavaPlugin() {
    override fun onEnable() {
        plugin = this

        saveDefaultConfig()
        applyRuntimeConfig(loadConfig())
        scheduler = Scheduler(this)
        coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        setupDatabase()
        registerActions()
        registerCommands()
        registerListeners()
        startTasks()

        installBetterModelResources()
    }

    override fun onDisable() {
        coroutineScope.cancel()
    }

    fun reloadRuntimeConfig() {
        reloadConfig()
        applyRuntimeConfig(loadConfig())
    }

    private fun loadConfig(): BloodRPConfig =
        BloodRPConfigLoader.load(getConfig())

    private fun applyRuntimeConfig(config: BloodRPConfig) {
        Companion.config = config
    }

    private fun registerActions() {
        ActionMaster.register("bj", BlowjobAnimation())
        ActionMaster.register("doggy", DoggyAnimation())
        ActionMaster.register("kiss", KissAnimation())
        ActionMaster.register("marry", MarryAnimation())
    }

    private fun setupDatabase() {
        if (!dataFolder.exists()) {
            dataFolder.mkdirs()
        }

        val databaseFile = File(dataFolder, "data").absolutePath
        Database.connect("jdbc:h2:file:$databaseFile;DB_CLOSE_DELAY=-1;CACHE_SIZE=8192", driver = "org.h2.Driver")

        transaction {
            SchemaUtils.create(MarriagesTable)
        }
    }

    private fun registerCommands() {
        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
            BloodRPCommandRegistrar.register(event.registrar())
        }
    }

    private fun registerListeners() {
        server.pluginManager.registerEvents(MarriageKissListener, this)
        server.pluginManager.registerEvents(MarriagePdcCleanupListener, this)
    }

    private fun startTasks() {
        MarriageInteractionExpirationTask.start()
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
        lateinit var plugin: BloodRP
            private set

        lateinit var config: BloodRPConfig
        lateinit var scheduler: Scheduler
        lateinit var coroutineScope: CoroutineScope

        private val betterModelModels = listOf(
            "bloodsex_doggy_active",
            "bloodsex_doggy_passive",
        )
    }
}
