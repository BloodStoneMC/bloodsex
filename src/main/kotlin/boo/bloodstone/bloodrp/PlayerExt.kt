package boo.bloodstone.bloodrp

import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import java.util.UUID

internal fun Player.marriagePartnerUuid(): UUID? {
    val rawUuid = persistentDataContainer.get(MARRIAGE_PARTNER_UUID, PersistentDataType.STRING)
        ?: return null
    return runCatching { UUID.fromString(rawUuid) }.getOrNull()
}

internal fun Player.setMarriagePartner(partner: Player) {
    persistentDataContainer.set(
        MARRIAGE_PARTNER_UUID,
        PersistentDataType.STRING,
        partner.uniqueId.toString()
    )
}

internal fun Player.clearMarriagePartner() {
    persistentDataContainer.remove(MARRIAGE_PARTNER_UUID)
}
