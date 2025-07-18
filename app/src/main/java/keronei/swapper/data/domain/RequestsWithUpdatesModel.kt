package keronei.swapper.data.domain

data class RequestsWithUpdatesModel(
    val request: BatteryRequestModel,
    val updates: List<BatteryRequestUpdateModel>
)
