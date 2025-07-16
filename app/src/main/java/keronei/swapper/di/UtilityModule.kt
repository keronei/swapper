package keronei.swapper.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import keronei.swapper.auth.LocationProvider
import keronei.swapper.auth.LocationUtil

@Module
@InstallIn(SingletonComponent::class)
object UtilityModule {

    @Provides
    fun provideLocationUtility(
        @ApplicationContext context: Context
    ): LocationUtil {
        return LocationProvider(context)
    }
}