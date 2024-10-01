package com.ibm.rides.domain.repository

import com.ibm.rides.domain.model.Vehicle

interface VehicleRepository {
    suspend fun getVehicles(size: Int): Result<List<Vehicle>>
}