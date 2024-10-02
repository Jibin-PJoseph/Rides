package com.ibm.rides.presentation.viewmodel

import com.ibm.rides.domain.usecase.CalculateEmissionsUseCase
import com.ibm.rides.domain.usecase.CountValidationResult
import com.ibm.rides.domain.usecase.SortVehiclesUseCase
import com.ibm.rides.domain.usecase.ValidateCountUseCase
import com.ibm.rides.domain.usecase.VehiclesUseCase
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
    private lateinit var viewModel: VehicleViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        calculateEmissionsUseCase = mockk()
        every { calculateEmissionsUseCase.calculate(5000) } returns 5000.0
        every { calculateEmissionsUseCase.calculate(6000) } returns 6500.0
        every { calculateEmissionsUseCase.calculate(3000) } returns 3000.0
        every { calculateEmissionsUseCase.calculate(-100) } returns 0.0
        viewModel = VehicleViewModel(
            vehicleUseCase,
            calculateEmissionsUseCase,
            validateCountUseCase,
            sortVehiclesUseCase
        )
    }

    @Test
    fun `test input within valid range triggers API call`() = runTest {
        every { validateCountUseCase.validate("50") } returns CountValidationResult(isValid = true, validatedCount = 50)
        viewModel.validateAndFetchVehicles("50")
        coVerify { vehicleUseCase.getVehicles(50) }
    }

    @Test
    fun `test input below valid range does not trigger API call`() = runTest {
        every { validateCountUseCase.validate("0") } returns CountValidationResult(isValid = false, errorMessage = "Please enter a number between 1 and 100.")
        viewModel.validateAndFetchVehicles("0")
        coVerify(exactly = 0) { vehicleUseCase.getVehicles(any()) }
        val event = viewModel.eventState.first()
        assertEquals("Please enter a number between 1 and 100.", event?.viewContent())
    }

    @Test
    fun `test input above valid range does not trigger API call`() = runTest {
        every { validateCountUseCase.validate("101") } returns CountValidationResult(isValid = false, errorMessage = "Please enter a number between 1 and 100.")
        viewModel.validateAndFetchVehicles("101")
        coVerify(exactly = 0) { vehicleUseCase.getVehicles(any()) }
        val event = viewModel.eventState.first()
        assertEquals("Please enter a number between 1 and 100.", event?.viewContent())
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
