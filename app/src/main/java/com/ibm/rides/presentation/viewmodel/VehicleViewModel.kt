package com.ibm.rides.presentation.viewmodel

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class VehicleViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val vehicleUseCase: VehiclesUseCase,
    private val calculateEmissionsUseCase: CalculateEmissionsUseCase,
    private val validateCountUseCase: ValidateCountUseCase,
    private val sortVehiclesUseCase: SortVehiclesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<VehicleUiState>(VehicleUiState.IdleState)
    val uiState: StateFlow<VehicleUiState> get() = _uiState

    private val _eventState = MutableStateFlow<Event<String>?>(null)
    val eventState: StateFlow<Event<String>?> get() = _eventState

    private var lastApiCallTime: Long = 0
    private val apiCallInterval: Long = 1000L
    private var currentVehicles: List<Vehicle> = emptyList()

    init {
        if (currentVehicles.isNotEmpty()) {
            _uiState.value = VehicleUiState.Success(currentVehicles)
        }
    }
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun validateAndFetchVehicles(countStr: String) {
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastApiCallTime < apiCallInterval) {
            return
        }
        lastApiCallTime = currentTime

        // Prevent multiple calls while loading
        if (_uiState.value is VehicleUiState.Loading) return

        val validation = validateCountUseCase.validate(countStr)
        if (validation.isValid) {
            viewModelScope.launch {
                fetchVehiclesWithProgressiveDelay(validation.validatedCount)
            }
        } else {
            _eventState.value = Event(validation.errorMessage)
            _uiState.value = if (currentVehicles.isNotEmpty()) {
                VehicleUiState.Success(currentVehicles)
            } else {
                VehicleUiState.IdleState
            }
        }
    }

    private suspend fun fetchVehiclesWithProgressiveDelay(size: Int, attempt: Int = 1) {
        val maxAttempts = 5
        val baseDelay = 1000L  // 1 second delay for progressive delay
        _uiState.value = VehicleUiState.Loading

        try {
            if (!isNetworkAvailable()) {
                if (currentVehicles.isNotEmpty()) {
                    _uiState.value = VehicleUiState.Error("No Internet Connection", currentVehicles)
                } else {
                    _uiState.value = VehicleUiState.Error("No Internet Connection")
                }
                return
            }

            val result = vehicleUseCase.getVehicles(size)

            result.fold(
                onSuccess = { vehicles ->
                    currentVehicles = vehicles
                    _uiState.value = VehicleUiState.Success(vehicles)
                },
                onFailure = { exception ->
                    if (exception.message?.contains("HTTP 429") == true && attempt < maxAttempts) {
                        val delayTime = baseDelay * attempt
                        delay(delayTime)
                        fetchVehiclesWithProgressiveDelay(size, attempt + 1)
                    } else {
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
                }
            )
        } catch (e: Exception) {
            if (currentVehicles.isNotEmpty()) {
                _uiState.value =
                    VehicleUiState.Error(e.message ?: "Something went wrong", currentVehicles)
            } else {
                _uiState.value = VehicleUiState.Error(e.message ?: "Something went wrong")
            }
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

//@HiltViewModel
//class VehicleViewModel @Inject constructor(
//    @ApplicationContext private val context: Context,
//    private val vehicleUseCase: VehiclesUseCase,
//    private val calculateEmissionsUseCase: CalculateEmissionsUseCase,
//    private val validateCountUseCase: ValidateCountUseCase,
//    private val sortVehiclesUseCase: SortVehiclesUseCase
//) : ViewModel() {
//
//    private val _uiState = MutableStateFlow<VehicleUiState>(VehicleUiState.IdleState)
//    val uiState: StateFlow<VehicleUiState> get() = _uiState
//
//    private val _eventState = MutableStateFlow<Event<String>?>(null)
//    val eventState: StateFlow<Event<String>?> get() = _eventState
//
//    private var lastApiCallTime: Long = 0
//    private val apiCallInterval: Long = 1000L
//    private var currentVehicles: List<Vehicle> = emptyList()
//    private var isApiCallInProgress = false
//
//    init {
//        if (currentVehicles.isNotEmpty()) {
//            _uiState.value = VehicleUiState.Success(currentVehicles)
//        }
//    }
//
//    fun resetUiState() {
//        _uiState.value = VehicleUiState.IdleState
//    }
//
//    private fun isNetworkAvailable(): Boolean {
//        val connectivityManager =
//            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//        val network = connectivityManager.activeNetwork ?: return false
//        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
//        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
//    }
//
//    fun validateAndFetchVehicles(countStr: String) {
//        val currentTime = System.currentTimeMillis()
//
//        if (currentTime - lastApiCallTime < apiCallInterval) {
//            return
//        }
//        lastApiCallTime = currentTime
//
//        // Prevent multiple calls while loading
//        if (_uiState.value is VehicleUiState.Loading) return
//
//        val validation = validateCountUseCase.validate(countStr)
//        if (validation.isValid) {
//            viewModelScope.launch {
//                fetchVehiclesWithProgressiveDelay(validation.validatedCount)
//            }
//        } else {
//            _eventState.value = Event(validation.errorMessage)
//            _uiState.value = if (currentVehicles.isNotEmpty()) {
//                VehicleUiState.Success(currentVehicles)
//            } else {
//                VehicleUiState.IdleState
//            }
//        }
//    }
//
////    fun validateAndFetchVehicles(countStr: String) {
////
////        val currentTime = System.currentTimeMillis()
////
////        // Flow Control to prevent rapid API calls
////        if (currentTime - lastApiCallTime < apiCallInterval) {
////            return // Skip API call if it's too soon according to flow control rules
////        }
////        lastApiCallTime = currentTime
////
////        if (_uiState.value is VehicleUiState.Loading) return
////
////        val validation = validateCountUseCase.validate(countStr)
////        if (validation.isValid) {
////            fetchVehiclesWithProgressiveDelay(validation.validatedCount)
////        } else {
////            _eventState.value = Event(validation.errorMessage)
////            _uiState.value = if (currentVehicles.isNotEmpty()) {
////                VehicleUiState.Success(currentVehicles)
////            } else {
////                VehicleUiState.IdleState
////            }
////        }
////    }
//
//
//    private suspend fun fetchVehiclesWithProgressiveDelay(size: Int, attempt: Int = 1) {
//        val maxAttempts = 5
//        val baseDelay = 1000L  // 1 second delay for progressive delay
//        _uiState.value = VehicleUiState.Loading
//
//        try {
//            if (!isNetworkAvailable()) {
//                _uiState.value = VehicleUiState.Error("No Internet Connection")
//                return
//            }
//
//            val result = vehicleUseCase.getVehicles(size)
//
//            result.fold(
//                onSuccess = { vehicles ->
//                    currentVehicles = vehicles
//                    _uiState.value = VehicleUiState.Success(vehicles)
//                },
//                onFailure = { exception ->
//                    // Check if it's an HTTP 429 (rate-limiting) error
//                    if (exception.message?.contains("HTTP 429") == true && attempt < maxAttempts) {
//                        val delayTime = baseDelay * attempt
//                        delay(delayTime)
//                        fetchVehiclesWithProgressiveDelay(size, attempt + 1)
//                    } else {
//                        _uiState.value = VehicleUiState.Error(exception.message ?: "Something went wrong")
//                    }
//                }
//            )
//        } catch (e: Exception) {
//            _uiState.value = VehicleUiState.Error(e.message ?: "Something went wrong")
//        }
//    }
////    private fun fetchVehicles(size: Int) {
////        val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
////            _uiState.value = VehicleUiState.Error(throwable.message ?: "Something went wrong")
////        }
////
////        _uiState.value = VehicleUiState.Loading
////
////        viewModelScope.launch(coroutineExceptionHandler) {
////            if (!isNetworkAvailable()) {
////                _uiState.value =
////                    VehicleUiState.Error("Please check connectivity: No Internet Connection")
////                return@launch//@HiltViewModel
////class VehicleViewModel @Inject constructor(
////    @ApplicationContext private val context: Context,
////    private val vehicleUseCase: VehiclesUseCase,
////    private val calculateEmissionsUseCase: CalculateEmissionsUseCase,
////    private val validateCountUseCase: ValidateCountUseCase,
////    private val sortVehiclesUseCase: SortVehiclesUseCase
////) : ViewModel() {
////
////    private val _uiState = MutableStateFlow<VehicleUiState>(VehicleUiState.IdleState)
////    val uiState: StateFlow<VehicleUiState> get() = _uiState
////
////    private val _eventState = MutableStateFlow<Event<String>?>(null)
////    val eventState: StateFlow<Event<String>?> get() = _eventState
////
////    private var lastApiCallTime: Long = 0
////    private val apiCallInterval: Long = 1000L
////    private var currentVehicles: List<Vehicle> = emptyList()
////    private var isApiCallInProgress = false
////
////    init {
////        if (currentVehicles.isNotEmpty()) {
////            _uiState.value = VehicleUiState.Success(currentVehicles)
////        }
////    }
////
////    fun resetUiState() {
////        _uiState.value = VehicleUiState.IdleState
////    }
////
////    private fun isNetworkAvailable(): Boolean {
////        val connectivityManager =
////            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
////        val network = connectivityManager.activeNetwork ?: return false
////        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
////        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
////    }
////
////    fun validateAndFetchVehicles(countStr: String) {
////        val currentTime = System.currentTimeMillis()
////
////        if (currentTime - lastApiCallTime < apiCallInterval) {
////            return
////        }
////        lastApiCallTime = currentTime
////
////        // Prevent multiple calls while loading
////        if (_uiState.value is VehicleUiState.Loading) return
////
////        val validation = validateCountUseCase.validate(countStr)
////        if (validation.isValid) {
////            viewModelScope.launch {
////                fetchVehiclesWithProgressiveDelay(validation.validatedCount)
////            }
////        } else {
////            _eventState.value = Event(validation.errorMessage)
////            _uiState.value = if (currentVehicles.isNotEmpty()) {
////                VehicleUiState.Success(currentVehicles)
////            } else {
////                VehicleUiState.IdleState
////            }
////        }
////    }
////
//////    fun validateAndFetchVehicles(countStr: String) {
//////
//////        val currentTime = System.currentTimeMillis()
//////
//////        // Flow Control to prevent rapid API calls
//////        if (currentTime - lastApiCallTime < apiCallInterval) {
//////            return // Skip API call if it's too soon according to flow control rules
//////        }
//////        lastApiCallTime = currentTime
//////
//////        if (_uiState.value is VehicleUiState.Loading) return
//////
//////        val validation = validateCountUseCase.validate(countStr)
//////        if (validation.isValid) {
//////            fetchVehiclesWithProgressiveDelay(validation.validatedCount)
//////        } else {
//////            _eventState.value = Event(validation.errorMessage)
//////            _uiState.value = if (currentVehicles.isNotEmpty()) {
//////                VehicleUiState.Success(currentVehicles)
//////            } else {
//////                VehicleUiState.IdleState
//////            }
//////        }
//////    }
////
////
////    private suspend fun fetchVehiclesWithProgressiveDelay(size: Int, attempt: Int = 1) {
////        val maxAttempts = 5
////        val baseDelay = 1000L  // 1 second delay for progressive delay
////        _uiState.value = VehicleUiState.Loading
////
////        try {
////            if (!isNetworkAvailable()) {
////                _uiState.value = VehicleUiState.Error("No Internet Connection")
////                return
////            }
////
////            val result = vehicleUseCase.getVehicles(size)
////
////            result.fold(
////                onSuccess = { vehicles ->
////                    currentVehicles = vehicles
////                    _uiState.value = VehicleUiState.Success(vehicles)
////                },
////                onFailure = { exception ->
////                    // Check if it's an HTTP 429 (rate-limiting) error
////                    if (exception.message?.contains("HTTP 429") == true && attempt < maxAttempts) {
////                        val delayTime = baseDelay * attempt
////                        delay(delayTime)
////                        fetchVehiclesWithProgressiveDelay(size, attempt + 1)
////                    } else {
////                        _uiState.value = VehicleUiState.Error(exception.message ?: "Something went wrong")
////                    }
////                }
////            )
////        } catch (e: Exception) {
////            _uiState.value = VehicleUiState.Error(e.message ?: "Something went wrong")
////        }
////    }
//////    private fun fetchVehicles(size: Int) {
//////        val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
//////            _uiState.value = VehicleUiState.Error(throwable.message ?: "Something went wrong")
//////        }
//////
//////        _uiState.value = VehicleUiState.Loading
//////
//////        viewModelScope.launch(coroutineExceptionHandler) {
//////            if (!isNetworkAvailable()) {
//////                _uiState.value =
//////                    VehicleUiState.Error("Please check connectivity: No Internet Connection")
//////                return@launch
//////            }
//////            val result = vehicleUseCase.getVehicles(size)
//////
//////            result.fold(
//////                onSuccess = { vehicles ->
//////                    currentVehicles = vehicles
//////                    _uiState.value = VehicleUiState.Success(vehicles)
//////                },
//////                onFailure = { exception ->
//////                    _uiState.value =
//////                        VehicleUiState.Error(exception.message ?: "Something went wrong")
//////                }
//////            )
//////        }
//////    }
////
////    fun calculateAndSetEmissions(kilometrage: Int) {
////        val emissions = calculateEmissionsUseCase.calculate(kilometrage)
////        _uiState.value = VehicleUiState.CarbonEmissionsSuccess(emissions)
////    }
////
////    fun sortVehiclesByCarType() {
////        if (currentVehicles.isNotEmpty()) {
////            val sortedVehicles = sortVehiclesUseCase.sortByCarType(currentVehicles)
////            _uiState.value = VehicleUiState.Success(sortedVehicles)
////        }
////    }
////}
////            }
////            val result = vehicleUseCase.getVehicles(size)
////
////            result.fold(
////                onSuccess = { vehicles ->
////                    currentVehicles = vehicles
////                    _uiState.value = VehicleUiState.Success(vehicles)
////                },
////                onFailure = { exception ->
////                    _uiState.value =
////                        VehicleUiState.Error(exception.message ?: "Something went wrong")
////                }
////            )
////        }
////    }
//
//    fun calculateAndSetEmissions(kilometrage: Int) {
//        val emissions = calculateEmissionsUseCase.calculate(kilometrage)
//        _uiState.value = VehicleUiState.CarbonEmissionsSuccess(emissions)
//    }
//
//    fun sortVehiclesByCarType() {
//        if (currentVehicles.isNotEmpty()) {
//            val sortedVehicles = sortVehiclesUseCase.sortByCarType(currentVehicles)
//            _uiState.value = VehicleUiState.Success(sortedVehicles)
//        }
//    }
//}
