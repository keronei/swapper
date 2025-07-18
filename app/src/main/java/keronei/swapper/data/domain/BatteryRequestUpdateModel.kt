package keronei.swapper.data.domain

class BatteryRequestUpdateModel(
    val id: Long?,
    val batteryCount: Int,
    val batteries: List<String>,
    val requestId: Long,
    val comment: String,
    val time: String
)
