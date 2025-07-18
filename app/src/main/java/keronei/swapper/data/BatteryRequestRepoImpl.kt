package keronei.swapper.data

import keronei.swapper.data.domain.BatteryRequestModel
import keronei.swapper.data.domain.BatteryRequestRepository
import keronei.swapper.data.domain.BatteryRequestUpdateModel
import keronei.swapper.data.domain.RequestsWithUpdatesModel
import keronei.swapper.data.local.BatteryRequestDao
import keronei.swapper.data.local.BatteryRequestUpdateEntity
import keronei.swapper.data.local.BatteryRequestsEntity
import keronei.swapper.utils.RequestResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BatteryRequestRepoImpl @Inject constructor(
    private val batteryRequestDao: BatteryRequestDao
) : BatteryRequestRepository {
    override suspend fun createBatteryRequest(requestModel: BatteryRequestModel): RequestResult<String> {
        try {

            batteryRequestDao.createBatteryRequest(
                BatteryRequestsEntity(
                    id = 0,
                    comment = requestModel.comment,
                    requestedCount = requestModel.requestCount,
                    createdTime = requestModel.createdTime,
                    status = requestModel.status,
                    synced = false
                )
            )

            return RequestResult.Success("Request created.")
        } catch (exception: Exception) {
            exception.printStackTrace()
            return RequestResult.Error(exception.localizedMessage ?: "An error occurred.")
        }
    }

    override suspend fun removeBatteryRequest(requestData: BatteryRequestModel): RequestResult<String> {
        try {
            val removed = requestData.id?.let { batteryRequestDao.removeBatteryRequest(it) }
            return if ((removed ?: 0) > 0) {
                RequestResult.Success("Removed")
            } else {
                RequestResult.Error("Failed to remove.")
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
            return RequestResult.Error(exception.localizedMessage ?: "An error occurred removing request.")
        }
    }

    override suspend fun updateBatteryRequest(updateModel: BatteryRequestUpdateModel): RequestResult<String> {
        try {
            val result = batteryRequestDao.updateBatteryRequest(
                BatteryRequestUpdateEntity(
                    id = 0,
                    batteryCount = updateModel.batteryCount,
                    batteries = updateModel.batteries,
                    requestId = updateModel.requestId,
                    comment = updateModel.comment,
                    updateAt = updateModel.time
                )
            )

            return if (result > 0) {
                RequestResult.Success("Update added")
            } else {
                RequestResult.Error("Failed to update.")
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
            return RequestResult.Error(exception.localizedMessage ?: "An error occurred updating request.")
        }
    }

    override fun queryLocalRequests(): Flow<List<RequestsWithUpdatesModel>> = batteryRequestDao.getLocalRequests().map { requests ->
        requests.map { singleItem ->
            val req = singleItem.requestsEntity

            RequestsWithUpdatesModel(
                BatteryRequestModel(
                    id = req.id,
                    comment = req.comment,
                    requestCount = req.requestedCount,
                    createdTime = req.createdTime,
                    status = req.status
                ), singleItem.updates.map { update ->
                    BatteryRequestUpdateModel(
                        id = update.id,
                        batteryCount = update.batteryCount,
                        batteries = update.batteries,
                        requestId = update.requestId,
                        comment = update.comment,
                        time = update.updateAt
                    )
                })
        }
    }


}