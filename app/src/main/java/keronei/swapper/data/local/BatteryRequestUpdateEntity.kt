package keronei.swapper.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [ForeignKey(entity = BatteryRequestsEntity::class, parentColumns = ["id"], childColumns = ["requestId"])]
)
class BatteryRequestUpdateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val batteryCount: Int,
    val batteries: String,
    val requestId: Long,
    val comment: String,
    val updateAt: String
)