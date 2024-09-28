package com.ibm.rides.domain.usecase

import com.ibm.rides.domain.model.Vehicle
import com.ibm.rides.domain.repository.VehicleRepository
import javax.inject.Inject

class VehiclesUSeCaseImpl @Inject constructor(
    private val vehicleRepository: VehicleRepository
): VehiclesUseCase {

    override suspend fun getVehicles(size: Int): Result<List<Vehicle>> {
        return vehicleRepository.getVehicles(size)
    }
}