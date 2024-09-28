package com.ibm.rides.data.repository

import com.ibm.rides.data.datasource.remote.RandomVehicleApi
import com.ibm.rides.data.datasource.remote.RemoteDataSource
import com.ibm.rides.domain.mapper.VehicleResponseDtoMapper
import com.ibm.rides.domain.model.Vehicle
import com.ibm.rides.domain.repository.VehicleRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.net.ConnectException
import java.net.UnknownHostException
import javax.inject.Inject

class VehicleRepositoryImpl @Inject constructor(
    private val randomVehicleApi: RandomVehicleApi,
    private val remoteDataSource: RemoteDataSource,
    private val responseDtoMapper: VehicleResponseDtoMapper,
    private val coroutineDispatcher: CoroutineDispatcher,

    ) : VehicleRepository {
    override suspend fun getVehicles(size: Int): Result<List<Vehicle>> {

        return withContext(coroutineDispatcher) {
            runCatching {
                val randomVehiclesDto = remoteDataSource.fetchRandomVehicles(size)
                responseDtoMapper.map(randomVehiclesDto)
            }.onFailure { exception ->
                handleError(exception)
            }
        }
    }

    private fun handleError(exception: Throwable): String {
        return when (exception) {
            is UnknownHostException, is ConnectException -> NO_INTERNET_ERROR
            is HttpException -> VEHICLE_LIST_UNAVAILABLE_ERROR
            else -> exception.message ?: "Unknown error occurred"
        }
    }

    private companion object {
        const val VEHICLE_LIST_UNAVAILABLE_ERROR = "Unable to get the vehicle list at this time."
        const val NO_INTERNET_ERROR = "Unable to connect, please check your internet connection."
    }
}

