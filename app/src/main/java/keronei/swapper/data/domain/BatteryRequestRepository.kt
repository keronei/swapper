package keronei.swapper.data.domain

import keronei.swapper.utils.RequestResult
import kotlinx.coroutines.flow.Flow

interface BatteryRequestRepository {
    suspend fun createBatteryRequest(requestModel: BatteryRequestModel): RequestResult<String>

    suspend fun removeBatteryRequest(requestId: BatteryRequestModel): RequestResult<String>

    suspend fun updateBatteryRequest(updateModel: BatteryRequestUpdateModel): RequestResult<String>

    fun queryLocalRequests(): Flow<List<RequestsWithUpdatesModel>>
}