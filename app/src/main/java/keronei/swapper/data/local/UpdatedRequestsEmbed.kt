package keronei.swapper.data.local

import androidx.room.Embedded
import androidx.room.Relation

data class UpdatedRequestsEmbed(
    @Embedded
    val requestsEntity: BatteryRequestsEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "requestId"
    )
    val updates: List<BatteryRequestUpdateEntity>
)
