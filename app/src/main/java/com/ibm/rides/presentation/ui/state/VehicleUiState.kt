package com.ibm.rides.presentation.ui.state

import com.ibm.rides.domain.model.Vehicle

sealed class VehicleUiState {
    object Loading : VehicleUiState()
    object IdleState : VehicleUiState()
    data class Success(val vehicles: List<Vehicle>) : VehicleUiState()
    data class ValidationError(val message: String) : VehicleUiState()
    data class CarbonEmissionsSuccess(val emissions: Double) : VehicleUiState()
    data class Error(val message: String) : VehicleUiState()
}
