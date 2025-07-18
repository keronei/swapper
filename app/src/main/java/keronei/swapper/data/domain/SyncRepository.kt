package keronei.swapper.data.domain

import keronei.swapper.utils.RequestResult

interface SyncRepository {
    suspend fun sendLocalRequests(requests: List<BatteryRequestModel>): RequestResult<String>

    suspend fun fetchRequestWithUpdates(): RequestResult<String>
}