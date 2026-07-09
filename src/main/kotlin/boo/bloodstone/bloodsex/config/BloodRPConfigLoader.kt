package boo.bloodstone.bloodsex.config

import org.bukkit.configuration.file.FileConfiguration

object BloodRPConfigLoader {
    fun load(config: FileConfiguration): BloodRPConfig =
        BloodRPConfig(
            maxActionDistance = config.getDouble("maxActionDistance", 16.0),
            maxRapeDistance = config.getDouble("maxRapeDistance", 5.0),
        )
}
