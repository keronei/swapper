package keronei.swapper.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import keronei.swapper.data.BatteryRequestRepoImpl
import keronei.swapper.data.SyncRepositoryImpl
import keronei.swapper.data.domain.BatteryRequestRepository
import keronei.swapper.data.local.BatteryRequestDao
import keronei.swapper.data.remote.ApiService

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    fun providesBatteryRequestRepository(batteryRequestDao: BatteryRequestDao):
            BatteryRequestRepository = BatteryRequestRepoImpl(batteryRequestDao)

    @Provides
    fun providesSyncRepository(
        apiService: ApiService, batteryRequestDao: BatteryRequestDao
    ): SyncRepositoryImpl {
        return SyncRepositoryImpl(apiService, batteryRequestDao)
    }
}