package boo.bloodstone.bloodrp

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

class MarriagePdcReconciliationTest {
    @Test
    fun `PDC is updated only when it differs from the database`() {
        val databasePartnerId = UUID.randomUUID()

        assertFalse(marriagePartnerNeedsUpdate(databasePartnerId.toString(), databasePartnerId))
        assertFalse(marriagePartnerNeedsUpdate(null, null))
        assertTrue(marriagePartnerNeedsUpdate(null, databasePartnerId))
        assertTrue(marriagePartnerNeedsUpdate("not-a-uuid", databasePartnerId))
        assertTrue(marriagePartnerNeedsUpdate(UUID.randomUUID().toString(), databasePartnerId))
        assertTrue(marriagePartnerNeedsUpdate(databasePartnerId.toString(), null))
        assertTrue(marriagePartnerNeedsUpdate("", null))
    }
}
