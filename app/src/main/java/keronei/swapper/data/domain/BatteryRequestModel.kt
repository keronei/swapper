package keronei.swapper.data.domain

data class BatteryRequestModel(
    val id: Int?,
    val comment: String,
    val requestCount: Int,
    val createdTime: String,
    val status: String
)
