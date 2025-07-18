package keronei.swapper.data.domain

data class BatteryRequestModel(
    val id: Long?,
    val comment: String,
    val requestCount: Int,
    val createdTime: String,
    val synced: Boolean,
    val requestedByStation: String,
    val status: String
)
