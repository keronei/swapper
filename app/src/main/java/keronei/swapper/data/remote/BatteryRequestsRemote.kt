package keronei.swapper.data.remote

import com.google.gson.annotations.SerializedName

data class BatteryRequestsRemote (
    val comment: String,
    @SerializedName("creation_time")
    val createdTime: String,
    val status: String,
    val batteriesCount: Int,
    val updates: List<BatteryRequestUpdatesRemote>
)