package com.example.moneymeter.util

import java.util.Date

object ValidationUtil {
    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String = ""
    )

    fun validateTransactionInput(
        title: String,
        amount: String,
        category: String,
        date: Date?
    ): ValidationResult {
        if (title.isBlank()) {
            return ValidationResult(false, "Title cannot be empty")
        }

        if (amount.isBlank()) {
            return ValidationResult(false, "Amount cannot be empty")
        }

        try {
            val amountValue = amount.toDouble()
            if (amountValue <= 0) {
                return ValidationResult(false, "Amount must be greater than 0")
            }
        } catch (e: NumberFormatException) {
            return ValidationResult(false, "Invalid amount format")
        }

        if (category.isBlank()) {
            return ValidationResult(false, "Please select a category")
        }

        if (date == null) {
            return ValidationResult(false, "Please select a date")
        }

        return ValidationResult(true)
    }

    fun validateBudget(budget: String): ValidationResult {
        if (budget.isBlank()) {
            return ValidationResult(false, "Budget cannot be empty")
        }

        try {
            val budgetValue = budget.toDouble()
            if (budgetValue <= 0) {
                return ValidationResult(false, "Budget must be greater than 0")
            }
        } catch (e: NumberFormatException) {
            return ValidationResult(false, "Invalid budget format")
        }

        return ValidationResult(true)
    }

    fun validateCurrency(currency: String): ValidationResult {
        if (currency.isBlank()) {
            return ValidationResult(false, "Please select a currency")
        }
        return ValidationResult(true)
    }

    fun validatePassword(password: String): ValidationResult {
        if (password.isBlank()) {
            return ValidationResult(false, "Password cannot be empty")
        }

        if (password.length != 4) {
            return ValidationResult(false, "Password must be exactly 4 digits")
        }

        if (!password.matches(Regex("^[0-9]{4}$"))) {
            return ValidationResult(false, "Password must contain only numbers")
        }

        return ValidationResult(true)
    }

    fun validateCurrentPassword(input: String, storedPassword: String): ValidationResult {
        return when {
            input.isEmpty() -> ValidationResult(false, "Please enter your current PIN")
            input != storedPassword -> ValidationResult(false, "Current PIN is incorrect")
            else -> ValidationResult(true)
        }
    }

    fun validateNewPassword(newPassword: String, confirmPassword: String): ValidationResult {
        return when {
            newPassword.isEmpty() -> ValidationResult(false, "Please enter a new PIN")
            newPassword.length != 4 -> ValidationResult(false, "PIN must be 4 digits")
            !newPassword.all { it.isDigit() } -> ValidationResult(false, "PIN must contain only numbers")
            newPassword != confirmPassword -> ValidationResult(false, "PINs do not match")
            else -> ValidationResult(true)
        }
    }
} 