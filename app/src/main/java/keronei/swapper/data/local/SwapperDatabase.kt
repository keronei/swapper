package keronei.swapper.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [BatteryRequestsEntity::class, BatteryRequestUpdateEntity::class], version = 1)
abstract class SwapperDatabase : RoomDatabase() {
    abstract fun batteryRequestDao(): BatteryRequestDao
}