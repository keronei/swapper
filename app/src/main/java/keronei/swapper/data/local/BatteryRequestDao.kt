package keronei.swapper.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BatteryRequestDao {
    @Insert
    suspend fun createBatteryRequest(batteryRequest: BatteryRequestsEntity): Long

    @Insert
    suspend fun updateBatteryRequest(batteryRequestUpdate: BatteryRequestUpdateEntity): Long

    @Query("DELETE from batteryrequestsentity WHERE id = :batteryRequestId")
    suspend fun removeBatteryRequest(batteryRequestId: Long): Int

    @Query("SELECT * from batteryrequestsentity")
    fun getLocalRequests(): Flow<List<UpdatedRequestsEmbed>>

}