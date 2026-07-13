package boo.bloodstone.bloodrp

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.concurrent.atomic.AtomicReference

class MutableClock private constructor(
    private val currentInstant: AtomicReference<Instant>,
    private val zoneId: ZoneId,
) : Clock() {
    constructor(initialInstant: Instant, zoneId: ZoneId = ZoneOffset.UTC) : this(AtomicReference(initialInstant), zoneId)

    override fun getZone(): ZoneId = zoneId

    override fun withZone(zone: ZoneId): Clock = MutableClock(currentInstant, zone)

    override fun instant(): Instant = currentInstant.get()

    fun advance(duration: Duration) {
        currentInstant.updateAndGet { instant -> instant.plus(duration) }
    }
}
