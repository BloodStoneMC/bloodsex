package boo.bloodstone.bloodrp.config

data class BloodRPConfig(
    val maxActionDistance: Double,
    val maxRapeDistance: Double,
    val topMarriagesHeader: String,
    val topMarriagesFooter: String,
    val maxDaysWithoutInteraction: Long,
    val warningDaysWithoutInteraction: Long,
    val sneakKissCooldownSeconds: Long,
    val policeGroup: String,
)
