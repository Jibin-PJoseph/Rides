package com.ibm.rides.presentation.ui.adapter

import com.ibm.rides.domain.model.Vehicle

interface VehicleSelectListener {
    fun onVehicleSelected(vehicle: Vehicle)
}