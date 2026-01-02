package com.example.moneymeter.data

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREF_NAME, Context.MODE_PRIVATE
    )

    var isFirstLaunch: Boolean
        get() = sharedPreferences.getBoolean(KEY_FIRST_LAUNCH, true)
        set(value) = sharedPreferences.edit().putBoolean(KEY_FIRST_LAUNCH, value).apply()

    var monthlyBudget: Double
        get() = sharedPreferences.getFloat(KEY_MONTHLY_BUDGET, 0f).toDouble()
        set(value) = sharedPreferences.edit().putFloat(KEY_MONTHLY_BUDGET, value.toFloat()).apply()

    var preferredCurrency: String
        get() = sharedPreferences.getString(KEY_PREFERRED_CURRENCY, "USD") ?: "USD"
        set(value) = sharedPreferences.edit().putString(KEY_PREFERRED_CURRENCY, value).apply()

    var lastBackupDate: Long
        get() = sharedPreferences.getLong(KEY_LAST_BACKUP, 0L)
        set(value) = sharedPreferences.edit().putLong(KEY_LAST_BACKUP, value).apply()

    var isNotificationsEnabled: Boolean
        get() = sharedPreferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
        set(value) = sharedPreferences.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, value).apply()

    var hasSetPassword: Boolean
        get() = sharedPreferences.getBoolean(KEY_HAS_PASSWORD, false)
        set(value) = sharedPreferences.edit().putBoolean(KEY_HAS_PASSWORD, value).apply()

    var password: String
        get() = sharedPreferences.getString(KEY_PASSWORD, "") ?: ""
        set(value) = sharedPreferences.edit().putString(KEY_PASSWORD, value).apply()

    companion object {
        private const val PREF_NAME = "MoneyMeterPrefs"
        private const val KEY_FIRST_LAUNCH = "first_launch"
        private const val KEY_MONTHLY_BUDGET = "monthly_budget"
        private const val KEY_PREFERRED_CURRENCY = "preferred_currency"
        private const val KEY_LAST_BACKUP = "last_backup"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_HAS_PASSWORD = "has_password"
        private const val KEY_PASSWORD = "password"
    }
} 