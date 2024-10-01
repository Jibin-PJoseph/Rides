package com.ibm.rides.presentation.viewmodel

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
): ViewModel() {

    private val _uiState = MutableStateFlow<State>(State.Loading)
    val uiState: StateFlow<State> get() = _uiState
    private var currentVehicles: List<Vehicle> = emptyList()

    fun fetchVehicles(size: Int) {

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

sealed interface State{
    data class VehicleSuccess(val vehicles: List<Vehicle>) : State
    data class VehiclesError(val errorMessage:  String) : State
    object Loading : State
}