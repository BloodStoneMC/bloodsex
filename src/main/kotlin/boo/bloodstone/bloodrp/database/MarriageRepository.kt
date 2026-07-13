package boo.bloodstone.bloodrp.database

import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.UUID
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

object MarriageRepository {
    fun initializeSchema() = transaction {
        SchemaUtils.create(MarriagesTable, MarriageParticipantsTable)
    }

    @OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)
    fun createMarriage(husbandId: UUID, wifeId: UUID, now: Instant): Set<UUID> {
        require(husbandId != wifeId) { "A player cannot marry themselves" }

        return try {
            transaction {
                val marriedPlayerIds = queryMarriedPlayers(husbandId, wifeId)
                if (marriedPlayerIds.isNotEmpty()) return@transaction marriedPlayerIds

                val marriageId = MarriagesTable.insertAndGetId {
                    it[MarriagesTable.husband] = husbandId.toKotlinUuid()
                    it[MarriagesTable.wife] = wifeId.toKotlinUuid()
                    it[MarriagesTable.startedAt] = now
                    it[MarriagesTable.lastInteractionAt] = now
                }
                listOf(husbandId, wifeId).forEach { playerId ->
                    MarriageParticipantsTable.insert {
                        it[MarriageParticipantsTable.playerId] = playerId.toKotlinUuid()
                        it[MarriageParticipantsTable.marriageId] = marriageId
                    }
                }
                emptySet<UUID>()
            }
        } catch (exception: Exception) {
            val marriedPlayerIds = runCatching { findMarriedPlayers(husbandId, wifeId) }
                .getOrElse { verificationFailure ->
                    exception.addSuppressed(verificationFailure)
                    throw exception
                }
            if (marriedPlayerIds.isEmpty()) throw exception
            marriedPlayerIds
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun findMarriedPlayers(firstPlayerId: UUID, secondPlayerId: UUID): Set<UUID> = transaction(readOnly = true) {
        queryMarriedPlayers(firstPlayerId, secondPlayerId)
    }

    @OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)
    fun findMarriage(playerId: UUID): PlayerMarriage? = transaction(readOnly = true) {
        val id = playerId.toKotlinUuid()
        val row = MarriagesTable
            .select(MarriagesTable.husband, MarriagesTable.wife, MarriagesTable.lastInteractionAt)
            .where((MarriagesTable.husband eq id) or (MarriagesTable.wife eq id))
            .limit(1)
            .singleOrNull() ?: return@transaction null

        val partnerId = if (row[MarriagesTable.husband] == id) row[MarriagesTable.wife] else row[MarriagesTable.husband]
        PlayerMarriage(partnerId.toJavaUuid(), row[MarriagesTable.lastInteractionAt])
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun queryMarriedPlayers(firstPlayerId: UUID, secondPlayerId: UUID): Set<UUID> {
        val firstId = firstPlayerId.toKotlinUuid()
        val secondId = secondPlayerId.toKotlinUuid()
        return MarriageParticipantsTable
            .select(MarriageParticipantsTable.playerId)
            .where((MarriageParticipantsTable.playerId eq firstId) or (MarriageParticipantsTable.playerId eq secondId))
            .mapTo(linkedSetOf()) { it[MarriageParticipantsTable.playerId].toJavaUuid() }
    }
}
