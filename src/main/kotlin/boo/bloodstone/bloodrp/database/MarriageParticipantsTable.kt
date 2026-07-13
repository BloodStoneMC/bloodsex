package boo.bloodstone.bloodrp.database

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
object MarriageParticipantsTable : Table("marriage_participants") {
    val playerId = uuid("player_uuid")
    val marriageId = reference("marriage_id", MarriagesTable, onDelete = ReferenceOption.CASCADE)

    override val primaryKey = PrimaryKey(playerId, name = "pk_marriage_participants")

    init {
        index(false, marriageId)
    }
}
