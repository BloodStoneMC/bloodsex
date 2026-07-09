package boo.bloodstone.bloodsex.database

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)
object MarriagesTable : Table("marriages") {
    val husband = uuid("husband")
    val wife = uuid("wife")
    val startedAt = timestamp("started_at")
    val lastInteractionAt = timestamp("last_interaction_at")

    init {
        index(false, husband)
        index(false, wife)
    }
}
