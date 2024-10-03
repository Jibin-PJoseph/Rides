package com.ibm.rides.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ibm.rides.data.NetworkChecker
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
    private val sortVehiclesUseCase: SortVehiclesUseCase,
    private val networkChecker: NetworkChecker,
    ) : ViewModel() {

    private val _uiState = MutableStateFlow<VehicleUiState>(VehicleUiState.IdleState)
    val uiState: StateFlow<VehicleUiState> get() = _uiState

    private val _eventState = MutableStateFlow<Event<String>?>(null)
    val eventState: StateFlow<Event<String>?> get() = _eventState

    private var currentVehicles: List<Vehicle> = emptyList()

    init {
        if (currentVehicles.isNotEmpty()) {
            _uiState.value = VehicleUiState.Success(currentVehicles)
        }
    }

    fun resetUiState() {
        _uiState.value = VehicleUiState.IdleState
    }

    fun validateAndFetchVehicles(countStr: String) {
        if (_uiState.value is VehicleUiState.Loading) return

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
        _uiState.value = VehicleUiState.Loading

        val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
            _uiState.value = VehicleUiState.Error(throwable.message ?: "Something went wrong")
        }

        viewModelScope.launch(coroutineExceptionHandler) {
            if (!networkChecker.isNetworkAvailable()) {
                if (currentVehicles.isNotEmpty()) {
                    _uiState.value = VehicleUiState.Error("No Internet Connection", currentVehicles)
                } else {
                    _uiState.value = VehicleUiState.Error("No Internet Connection")
                }
                return@launch
            }

            val result = vehicleUseCase.getVehicles(size)

            result.fold(
                onSuccess = { vehicles ->
                    currentVehicles = vehicles
                    _uiState.value = VehicleUiState.Success(vehicles)
                },
                onFailure = { exception ->
                    if (currentVehicles.isNotEmpty()) {
                        _uiState.value = VehicleUiState.Error(
                            exception.message ?: "Something went wrong",
                            currentVehicles
                        )
                    } else {
                        _uiState.value =
                            VehicleUiState.Error(exception.message ?: "Something went wrong")
                    }
                }
            )
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
