package com.ibm.rides.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ibm.rides.domain.model.Vehicle
import com.ibm.rides.domain.usecase.CalculateEmissionsUseCase
import com.ibm.rides.domain.usecase.SortVehiclesUseCase
import com.ibm.rides.domain.usecase.ValidateCountUseCase
import com.ibm.rides.domain.usecase.VehiclesUseCase
import com.ibm.rides.presentation.ui.state.VehicleUiState
import com.ibm.rides.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VehicleViewModel @Inject constructor(
    private val vehicleUseCase: VehiclesUseCase,
    private val calculateEmissionsUseCase: CalculateEmissionsUseCase,
    private val validateCountUseCase: ValidateCountUseCase,
    private val sortVehiclesUseCase: SortVehiclesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<VehicleUiState>(VehicleUiState.Loading)
    val uiState: StateFlow<VehicleUiState> get() = _uiState

    private val _eventState = MutableStateFlow<Event<String>?>(null)
    val eventState: StateFlow<Event<String>?> get() = _eventState


    private var currentVehicles: List<Vehicle> = emptyList()
    private var isApiCallInProgress = false

    fun validateAndFetchVehicles(countStr: String) {
        if (isApiCallInProgress) return

        val validation = validateCountUseCase.validate(countStr)
        if (validation.isValid) {
            fetchVehicles(validation.validatedCount)
        } else {
            _eventState.value = Event(validation.errorMessage)
            _uiState.value = if (currentVehicles.isNotEmpty()) {
                VehicleUiState.Success(currentVehicles)
            } else {
                VehicleUiState.IdleState
            }
        }
    }

    private fun fetchVehicles(size: Int) {
        isApiCallInProgress = true
        val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
            _uiState.value = VehicleUiState.Error(throwable.message ?: "Something went wrong")
        }

        _uiState.value = VehicleUiState.Loading

        viewModelScope.launch(coroutineExceptionHandler) {
            val result = vehicleUseCase.getVehicles(size)

            result.fold(
                onSuccess = { vehicles ->
                    currentVehicles = vehicles
                    _uiState.value = VehicleUiState.Success(vehicles)
                },
                onFailure = { exception ->
                    _uiState.value = VehicleUiState.Error(exception.message ?: "Something went wrong")
                }
            )
            isApiCallInProgress = false
        }
    }

    fun calculateAndSetEmissions(kilometrage: Int) {
        val emissions = calculateEmissionsUseCase.calculate(kilometrage)
        _uiState.value = VehicleUiState.CarbonEmissionsSuccess(emissions)
    }

    fun sortVehiclesByCarType() {
        if (currentVehicles.isNotEmpty()) {
            val sortedVehicles = sortVehiclesUseCase.sortByCarType(currentVehicles)
            _uiState.value = VehicleUiState.Success(sortedVehicles)
        }
    }
}
