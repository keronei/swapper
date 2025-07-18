package keronei.swapper.data.remote

import com.google.gson.annotations.SerializedName

data class BatteryRequestUpdatesRemote (
    @SerializedName("battery_count")
    val batteryCount: Int,
    val batteries: List<String>,
    @SerializedName("request_id")
    val requestId: Int,
    val comment: String,
    val status: String,
    val time: String
)