package com.ibm.rides.presentation.viewmodel

import com.ibm.rides.data.NetworkChecker
import com.ibm.rides.domain.model.Vehicle
import com.ibm.rides.domain.usecase.CalculateEmissionsUseCase
import com.ibm.rides.domain.usecase.CountValidationResult
import com.ibm.rides.domain.usecase.SortVehiclesUseCase
import com.ibm.rides.domain.usecase.ValidateCountUseCase
import com.ibm.rides.domain.usecase.VehiclesUseCase
import com.ibm.rides.presentation.ui.state.VehicleUiState
import com.ibm.rides.utils.ResourceManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VehicleViewModelTest {

    private val vehicleUseCase: VehiclesUseCase = mockk()
    private var calculateEmissionsUseCase: CalculateEmissionsUseCase = mockk()
    private val validateCountUseCase: ValidateCountUseCase = mockk()
    private val sortVehiclesUseCase: SortVehiclesUseCase = mockk()
    private val networkChecker: NetworkChecker = mockk()
    private val resourceManager: ResourceManager = mockk()
    private lateinit var viewModel: VehicleViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)


        every { calculateEmissionsUseCase.calculate(-100) } returns 0.0
        every { calculateEmissionsUseCase.calculate(3000) } returns 3000.0
        every { calculateEmissionsUseCase.calculate(6000) } returns 6500.0
        every { networkChecker.isNetworkAvailable() } returns true

        every { calculateEmissionsUseCase.calculate(5000) } returns 5000.0

        coEvery { vehicleUseCase.getVehicles(50) } returns Result.success(emptyList())

        coEvery { vehicleUseCase.getVehicles(50) } returns Result.success(
            listOf(
                Vehicle(
                    id = 1,
                    uid = "uniqueId",
                    vin = "VIN123",
                    makeAndModel = "Toyota Camry",
                    color = "Red",
                    transmission = "Automatic",
                    driveType = "FWD",
                    fuelType = "Gasoline",
                    carType = "Sedan",
                    carOptions = listOf("Sunroof", "Leather seats"),
                    specs = listOf("ABS", "Airbags"),
                    doors = 4,
                    mileage = 10000,
                    kilometrage = 16000,
                    licensePlate = "ABC123"
                )
            )
        )
        viewModel = VehicleViewModel(
            vehicleUseCase,
            calculateEmissionsUseCase,
            validateCountUseCase,
            sortVehiclesUseCase,
            networkChecker,
            resourceManager
        )

        viewModel.resetUiState()
    }

    @Test
    fun `test input within valid range triggers API call`() = runTest {
        every { validateCountUseCase.validate("50") } returns CountValidationResult(
            isValid = true,
            validatedCount = 50
        )
        viewModel.validateAndFetchVehicles("50")
        advanceUntilIdle()
        coVerify { vehicleUseCase.getVehicles(50) }
    }

    @Test
    fun `test input below valid range does not trigger API call`() = runTest {
        every { validateCountUseCase.validate("0") } returns CountValidationResult(
            isValid = false,
            errorMessage = "Please enter a number between 1 and 100."
        )
        viewModel.validateAndFetchVehicles("0")
        coVerify(exactly = 0) { vehicleUseCase.getVehicles(any()) }
        val event = viewModel.eventState.first()
        assertEquals("Please enter a number between 1 and 100.", event?.viewContent())
    }

    @Test
    fun `test input above valid range does not trigger API call`() = runTest {
        every { validateCountUseCase.validate("101") } returns CountValidationResult(
            isValid = false,
            errorMessage = "Please enter a number between 1 and 100."
        )
        viewModel.validateAndFetchVehicles("101")
        coVerify(exactly = 0) { vehicleUseCase.getVehicles(any()) }
        val event = viewModel.eventState.first()
        assertEquals("Please enter a number between 1 and 100.", event?.viewContent())
    }

    @Test
    fun `test API call returns a list of vehicles`() = runTest {
        every { validateCountUseCase.validate("50") } returns CountValidationResult(
            isValid = true,
            validatedCount = 50
        )
        viewModel.validateAndFetchVehicles("50")
        advanceUntilIdle()
        coVerify { vehicleUseCase.getVehicles(50) }
        assertTrue(viewModel.uiState.value is VehicleUiState.Success)
        val vehicles = (viewModel.uiState.value as VehicleUiState.Success).vehicles
        assertEquals(1, vehicles.size)
        assertEquals("Toyota Camry", vehicles[0].makeAndModel)
    }

    @Test
    fun `calculate emissions for negative kilometrage`() {
        val result = calculateEmissionsUseCase.calculate(-100)
        assertEquals(0.0, result, 0.0)
    }

    @Test
    fun `calculate emissions for kilometrage less than 5000`() {
        val result = calculateEmissionsUseCase.calculate(3000)
        assertEquals(3000.0, result, 0.0)
    }

    @Test
    fun `calculate emissions for kilometrage exactly 5000`() {
        val result = calculateEmissionsUseCase.calculate(5000)
        assertEquals(5000.0, result, 0.0)
    }

    @Test
    fun `calculate emissions for kilometrage greater than 5000`() {
        val result = calculateEmissionsUseCase.calculate(6000)
        assertEquals(6500.0, result, 0.0)
    }
}
