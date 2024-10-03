package com.ibm.rides.di

import android.content.Context
import com.ibm.rides.data.DefaultNetworkChecker
import com.ibm.rides.data.NetworkChecker
import com.ibm.rides.data.datasource.remote.RandomVehicleApi
import com.ibm.rides.data.datasource.remote.RemoteDataSource
import com.ibm.rides.data.repository.VehicleRepositoryImpl
import com.ibm.rides.domain.mapper.VehicleResponseDtoMapper
import com.ibm.rides.domain.repository.VehicleRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://random-data-api.com/api/vehicle/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    fun provideRemoteDataSource(
        vehicleApi: RandomVehicleApi
    ): RemoteDataSource {
        return RemoteDataSource(vehicleApi)
    }

    @Provides
    @Singleton
    fun provideRandomVehicleApi(retrofit: Retrofit): RandomVehicleApi {
        return retrofit.create(RandomVehicleApi::class.java)
    }

    @Provides
    @Singleton
    fun provideVehicleRepository(randomVehicleApi: RandomVehicleApi,
                                 remoteDataSource: RemoteDataSource,
                                 dispatcher: CoroutineDispatcher,
                                 responseDtoMapper: VehicleResponseDtoMapper

    ): VehicleRepository {
         return VehicleRepositoryImpl(
             randomVehicleApi,
             remoteDataSource,
             responseDtoMapper,
             dispatcher)
    }

    @Provides
    @Singleton
    fun provideNetworkChecker(
        @ApplicationContext context: Context
    ): NetworkChecker {
        return DefaultNetworkChecker(context)
    }
}
