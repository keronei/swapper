package keronei.swapper.data.remote

import com.google.gson.annotations.SerializedName

data class BatterySyncDTO(
    val comment: String,
    @SerializedName("creation_time")
    val createdTime: String,
    val requestedCount: Int
)
