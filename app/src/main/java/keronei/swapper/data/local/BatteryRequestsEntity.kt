package keronei.swapper.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class BatteryRequestsEntity (
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val comment: String,
    val requestedCount: Int,
    val createdTime: String,
    val status: String,
    val synced: Boolean
)
