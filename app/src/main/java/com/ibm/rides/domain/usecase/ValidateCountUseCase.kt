package com.ibm.rides.domain.usecase

class ValidateCountUseCase {
    fun validate(countStr: String): CountValidationResult {
        return try {
            val count = countStr.toInt()
            if (count in 1..100) {
                CountValidationResult(isValid = true, validatedCount = count)
            } else {
                CountValidationResult(
                    isValid = false,
                    errorMessage = "Please enter a number between 1 and 100."
                )
            }
        } catch (e: NumberFormatException) {
            CountValidationResult(isValid = false, errorMessage = "Please enter a valid number.")
        }
    }
}

data class CountValidationResult(
    val isValid: Boolean,
    val validatedCount: Int = 0,
    val errorMessage: String = ""
)