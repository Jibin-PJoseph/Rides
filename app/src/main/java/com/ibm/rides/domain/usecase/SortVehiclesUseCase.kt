package com.ibm.rides.domain.usecase

import com.ibm.rides.domain.model.Vehicle

class SortVehiclesUseCase {
    fun sortByCarType(vehicles: List<Vehicle>): List<Vehicle> {
        return vehicles.sortedBy { it.carType }
    }
}