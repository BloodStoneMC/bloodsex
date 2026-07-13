package boo.bloodstone.bloodrp.config

import org.bukkit.configuration.file.FileConfiguration

object BloodRPConfigLoader {
    fun load(config: FileConfiguration): BloodRPConfig =
        BloodRPConfig(
            maxActionDistance = config.getDouble("maxActionDistance", 16.0),
            maxRapeDistance = config.getDouble("maxRapeDistance", 5.0),
            topMarriagesHeader = config.getString(
                "topMarriages.header",
                "<gold><bold>Топ браков</bold></gold>"
            )!!,
            topMarriagesFooter = config.getString(
                "topMarriages.footer",
                "<gray>Новые браки попадают сюда автоматически.</gray>"
            )!!,
            maxDaysWithoutInteraction = config.getLong("maxDaysWithoutInteraction", 14L),
            warningDaysWithoutInteraction = config.getLong("warningDaysWithoutInteraction", 13L),
            sneakKissCooldownSeconds = config.getLong("sneakKissCooldownSeconds", 5L),
        )
}
