package keronei.swapper.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import keronei.swapper.data.remote.ApiService
import keronei.swapper.utils.baseUrl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    fun apiServiceBuilder(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    fun provideRetrofit(http: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder().baseUrl(baseUrl).addConverterFactory(
            GsonConverterFactory.create(
                gson
            )
        ).client(http).build()
    }

    @Provides
    fun buildGson(): Gson =
        GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss'Z'")
            .create()

    @Singleton
    @Provides
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient
            .Builder()
            .readTimeout(20, java.util.concurrent.TimeUnit.SECONDS)
            .build()
}