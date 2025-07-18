package keronei.swapper.data

import keronei.swapper.data.domain.BatteryRequestModel
import keronei.swapper.data.domain.SyncRepository
import keronei.swapper.data.local.BatteryRequestDao
import keronei.swapper.data.local.BatteryRequestUpdateEntity
import keronei.swapper.data.local.BatteryRequestsEntity
import keronei.swapper.data.remote.ApiService
import keronei.swapper.data.remote.BatterySyncDTO
import keronei.swapper.utils.RequestResult
import javax.inject.Inject

class SyncRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val batteryRequestDao: BatteryRequestDao
) : SyncRepository {
    override suspend fun sendLocalRequests(requests: List<BatteryRequestModel>): RequestResult<String> {
        try {
            val requestResult = apiService.sendLocalData(
                requests.map { entity ->
                    BatterySyncDTO(
                        comment = entity.comment,
                        createdTime = entity.createdTime,
                        requestedCount = entity.requestCount
                    )
                }
            )

            return if (requestResult.isSuccessful) {
                RequestResult.Success("Synced Successfully")
            } else {
                RequestResult.Error(requestResult.message())
            }

        } catch (exception: Exception) {
            exception.printStackTrace()
            return RequestResult.Error(exception.localizedMessage)
        }
    }

    override suspend fun fetchRequestWithUpdates(): RequestResult<String> {
        try {
            val request = apiService.fetchServerData()

            return if (request.isSuccessful) {

                val entries = request.body() ?: return RequestResult.Success("No data was found.")

                entries.forEach { fetchedRequest ->
                    val createdId = batteryRequestDao.createBatteryRequest(
                        batteryRequest = BatteryRequestsEntity(
                            id = 0,
                            comment = fetchedRequest.comment,
                            requestedCount = fetchedRequest.batteriesCount,
                            createdTime = fetchedRequest.createdTime,
                            status = fetchedRequest.status,
                            synced = true,
                            requestedByStation = fetchedRequest.requestedByStation
                        )
                    )

                    fetchedRequest.updates.forEach { update ->
                        batteryRequestDao.updateBatteryRequest(
                            BatteryRequestUpdateEntity(
                                id = 0,
                                batteryCount = update.batteryCount,
                                batteries = update.batteries.joinToString { "," },
                                requestId = createdId,
                                comment = update.comment,
                                updateAt = update.time
                            )
                        )
                    }
                }

                RequestResult.Success("Synced ${entries.size} successfully")
            } else {
                RequestResult.Error(request.message())
            }

        } catch (exception: Exception) {
            exception.printStackTrace()
            return RequestResult.Error(exception.localizedMessage)

        }
    }
}