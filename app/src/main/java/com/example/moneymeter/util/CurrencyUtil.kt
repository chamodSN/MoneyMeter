package com.example.moneymeter.util

import java.text.NumberFormat
import java.util.*

object CurrencyUtil {
    data class CurrencyInfo(
        val code: String,
        val symbol: String,
        val name: String
    )

    val availableCurrencies = listOf(
        CurrencyInfo("USD", "$", "US Dollar"),
        CurrencyInfo("EUR", "€", "Euro"),
        CurrencyInfo("GBP", "£", "British Pound"),
        CurrencyInfo("JPY", "¥", "Japanese Yen"),
        CurrencyInfo("AUD", "A$", "Australian Dollar"),
        CurrencyInfo("CAD", "C$", "Canadian Dollar"),
        CurrencyInfo("CHF", "Fr", "Swiss Franc"),
        CurrencyInfo("CNY", "¥", "Chinese Yuan"),
        CurrencyInfo("INR", "₹", "Indian Rupee"),
        CurrencyInfo("KRW", "₩", "South Korean Won")
    )

    fun formatAmount(amount: Double, currencyCode: String): String {
        val locale = when (currencyCode) {
            "EUR" -> Locale("en", "DE")  // Using German locale for Euro
            "GBP" -> Locale("en", "GB")
            "JPY" -> Locale("ja", "JP")
            "AUD" -> Locale("en", "AU")
            "CAD" -> Locale("en", "CA")
            "CHF" -> Locale("de", "CH")
            "CNY" -> Locale("zh", "CN")
            "INR" -> Locale("en", "IN")
            "KRW" -> Locale("ko", "KR")
            else -> Locale.US  // Default to US locale for USD
        }

        return NumberFormat.getCurrencyInstance(locale).apply {
            currency = java.util.Currency.getInstance(currencyCode)
        }.format(amount)
    }

    fun getCurrencyByCode(code: String): CurrencyInfo {
        return availableCurrencies.find { it.code == code }
            ?: CurrencyInfo("USD", "$", "US Dollar")
    }

    fun getCurrencyDisplayName(currency: CurrencyInfo): String {
        return "${currency.name} (${currency.symbol})"
    }
} 