package com.ibm.rides.domain.usecase

import com.ibm.rides.domain.model.Vehicle

interface VehiclesUseCase {

    suspend fun getVehicles(size: Int) : Result<List<Vehicle>>
}