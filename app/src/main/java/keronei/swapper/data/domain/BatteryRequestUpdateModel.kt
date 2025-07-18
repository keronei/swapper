package keronei.swapper.data.domain

class BatteryRequestUpdateModel(
    val id: Int?,
    val batteryCount: Int,
    val batteries: List<String>,
    val requestId: Int,
    val comment: String,
    val time: String
)
