package com.ibm.rides.domain.usecase

class CalculateEmissionsUseCase {
    fun calculate(kilometrage: Int): Double {
        return if (kilometrage <= 5000) {
            kilometrage * 1.0 // 1 unit per km for the first 5000 km
        } else {
            (5000 * 1.0) + ((kilometrage - 5000) * 1.5) // 1.5 units for km above 5000
        }
    }
}