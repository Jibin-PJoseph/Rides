package com.ibm.rides.data.datasource.remote

import com.ibm.rides.data.model.VehicleResponseDto
import javax.inject.Inject

class RemoteDataSource @Inject constructor(
    private val vehicleApi: RandomVehicleApi
): DataSource<List<VehicleResponseDto>?> {
    override suspend fun fetchRandomVehicles(size: Int): List<VehicleResponseDto>? {
        val response = vehicleApi.getRandomVehicle(size)
        return if (response.isEmpty() ){
            emptyList()
        } else {
            response.sortedBy { it.vin }
        }

    }
}