package keronei.swapper.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import keronei.swapper.data.local.BatteryRequestDao
import keronei.swapper.data.local.SwapperDatabase
import keronei.swapper.utils.DB_NAME
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): SwapperDatabase {
        return Room.databaseBuilder(context, SwapperDatabase::class.java, DB_NAME).build()
    }

    @Provides
    fun provideBatteryRequestDao(swapperDatabase: SwapperDatabase): BatteryRequestDao {
        return swapperDatabase.batteryRequestDao()
    }

}