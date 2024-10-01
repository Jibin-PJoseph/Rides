package com.ibm.rides.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ibm.rides.domain.model.Vehicle
import com.ibm.rides.domain.usecase.VehiclesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VehicleViewModel @Inject constructor(
    private val vehicleUseCase: VehiclesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<State>(State.Loading)
    val uiState: StateFlow<State> get() = _uiState

    private val _countValidationResult = MutableStateFlow<CountValidationResult?>(null)
    val validationResult: StateFlow<CountValidationResult?> get() = _countValidationResult

    private val _carbonEmissions = MutableStateFlow<Double>(0.0)
    val carbonEmissions: StateFlow<Double> get() = _carbonEmissions


    private var currentVehicles: List<Vehicle> = emptyList()

    private fun fetchVehicles(size: Int) {

        val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
            _uiState.value = State.VehiclesError(throwable.message ?: SOMETHING_WENT_WRONG)
        }

        viewModelScope.launch(coroutineExceptionHandler) {
            val result = vehicleUseCase.getVehicles(size)

            result.fold(
                onSuccess = { vehicles ->
                    currentVehicles = vehicles
                    _uiState.value = State.VehicleSuccess(vehicles)
                },
                onFailure = { exception ->
                    _uiState.value = State.VehiclesError(exception.message ?: "Something went wrong")
                }
            )

        }
    }

    fun validateAndFetchVehicles(countStr: String) {
        val validation = validateVehicleCount(countStr)
        _countValidationResult.value = validation
        if (validation.isValid) {
            fetchVehicles(validation.validatedCount)
        }
    }

    private fun validateVehicleCount(countStr: String): CountValidationResult {
        return try {
            val count = countStr.toInt()
            if (count in 1..100) {
                CountValidationResult(true, count)
            } else {
                CountValidationResult(
                    false,
                    errorMessage = "Please enter a number between 1 and 100."
                )
            }
        } catch (e: NumberFormatException) {
            CountValidationResult(false, errorMessage = "Please enter a valid number.")
        }
    }

    fun calculateAndSetEmissions(kilometrage: Int) {
        val emissions = calculateCarbonEmissions(kilometrage)
        _carbonEmissions.value = emissions
        Log.e("EstimatedCarbonEmission", "clacl")

    }

    private fun calculateCarbonEmissions(kilometrage: Int): Double {
        return if (kilometrage <= 5000) {
            kilometrage * 1.0 // 1 unit per km for the first 5000 km
        } else {
            (5000 * 1.0) + ((kilometrage - 5000) * 1.5) // 1.5 units for km above 5000
        }
    }
    data class CountValidationResult(
        val isValid: Boolean,
        val validatedCount: Int = 0,
        val errorMessage: String = ""
    )

    fun sortVehiclesByCarType() {
        if (currentVehicles.isNotEmpty()) {
            val sortedVehicles = currentVehicles.sortedBy { it.carType }
            _uiState.value = State.VehicleSuccess(sortedVehicles)
        }
    }

    private companion object {
        const val SOMETHING_WENT_WRONG = "Something went wrong"
    }
}

sealed interface State {
    data class VehicleSuccess(val vehicles: List<Vehicle>) : State
    data class VehiclesError(val errorMessage: String) : State
    object Loading : State
}