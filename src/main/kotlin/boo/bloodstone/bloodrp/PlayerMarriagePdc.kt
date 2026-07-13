package boo.bloodstone.bloodrp

import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import java.util.UUID

fun Player.marriagePartnerValue(): String? {
    if (MARRIAGE_PARTNER_UUID !in persistentDataContainer.keys) return null
    return runCatching {
        persistentDataContainer.get(MARRIAGE_PARTNER_UUID, PersistentDataType.STRING)
    }.getOrNull().orEmpty()
}

fun Player.marriagePartnerUuid(): UUID? = marriagePartnerValue()?.let { value ->
    runCatching { UUID.fromString(value) }.getOrNull()
}

fun marriagePartnerNeedsUpdate(storedValue: String?, databasePartnerId: UUID?): Boolean =
    storedValue != databasePartnerId?.toString()

fun Player.setMarriagePartner(partner: Player) = setMarriagePartner(partner.uniqueId)

fun Player.setMarriagePartner(partnerUuid: UUID) {
    persistentDataContainer.set(MARRIAGE_PARTNER_UUID, PersistentDataType.STRING, partnerUuid.toString())
}

fun Player.clearMarriagePartner() {
    persistentDataContainer.remove(MARRIAGE_PARTNER_UUID)
}
