package com.example.moneymeter.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.moneymeter.data.*
import com.example.moneymeter.util.BackupManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.Date

class TransactionViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TransactionRepository
    private val preferencesManager: PreferencesManager

    val allTransactions: LiveData<List<Transaction>>
    val totalIncome: LiveData<Double?>
    val totalExpense: LiveData<Double?>

    init {
        val dao = AppDatabase.getDatabase(application).transactionDao()
        repository = TransactionRepository(dao, application)
        preferencesManager = PreferencesManager(application)

        allTransactions = repository.allTransactions
        totalIncome = repository.getTotalByType(TransactionType.INCOME)
        totalExpense = repository.getTotalByType(TransactionType.EXPENSE)
    }

    fun insert(transaction: Transaction) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(transaction)
    }

    fun update(transaction: Transaction) = viewModelScope.launch(Dispatchers.IO) {
        repository.update(transaction)
    }

    fun delete(transaction: Transaction) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(transaction)
    }

    fun getTransactionsByType(type: TransactionType): LiveData<List<Transaction>> {
        return repository.getTransactionsByType(type)
    }

    fun getTransactionsBetweenDates(startDate: Date, endDate: Date): LiveData<List<Transaction>> {
        return repository.getTransactionsBetweenDates(startDate, endDate)
    }

    fun getCategoryTotals(type: TransactionType): LiveData<List<CategoryTotal>> {
        return repository.getCategoryTotals(type)
    }

    suspend fun exportData(): BackupManager.BackupResult {
        return repository.exportToJson()
    }

    suspend fun importData(filePath: String): BackupManager.RestoreResult {
        return repository.importFromJson(filePath)
    }

    suspend fun getAvailableBackups(): List<File> {
        return repository.getAvailableBackups()
    }

    // Preferences related methods
    fun isFirstLaunch(): Boolean = preferencesManager.isFirstLaunch

    fun setFirstLaunch(isFirst: Boolean) {
        preferencesManager.isFirstLaunch = isFirst
    }

    fun getMonthlyBudget(): Double = preferencesManager.monthlyBudget

    fun setMonthlyBudget(budget: Double) {
        preferencesManager.monthlyBudget = budget
    }

    fun getPreferredCurrency(): String = preferencesManager.preferredCurrency

    fun setPreferredCurrency(currency: String) {
        preferencesManager.preferredCurrency = currency
    }

    fun getLastBackupDate(): Long = preferencesManager.lastBackupDate
} 