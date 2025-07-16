package keronei.swapper.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import keronei.swapper.auth.LocationUtil

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AppDependencies {
    fun locationUtil(): LocationUtil
}
