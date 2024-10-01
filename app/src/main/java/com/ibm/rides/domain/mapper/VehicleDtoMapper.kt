package com.ibm.rides.domain.mapper

import com.ibm.rides.data.model.VehicleResponseDto
import com.ibm.rides.domain.NullableListDataMapper
import com.ibm.rides.domain.model.Vehicle

class VehicleResponseDtoMapper : NullableListDataMapper<VehicleResponseDto, Vehicle> {

    override fun map(list: List<VehicleResponseDto>?): List<Vehicle> {
        return list?.map { dto ->
            Vehicle(
                id = dto.id,
                uid = dto.uid,
                vin = dto.vin,
                makeAndModel = dto.makeAndModel,
                color = dto.color,
                transmission = dto.transmission,
                driveType = dto.driveType,
                fuelType = dto.fuelType,
                carType = dto.carType,
                carOptions = dto.carOptions,
                specs = dto.specs,
                doors = dto.doors,
                mileage = dto.mileage,
                kilometrage = dto.kilometrage,
                licensePlate = dto.licensePlate
            )
        } ?: emptyList()
    }
}
