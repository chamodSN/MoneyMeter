package com.example.moneymeter.data

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import com.example.moneymeter.util.BackupManager
import com.example.moneymeter.data.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date

class TransactionRepository(
    private val transactionDao: TransactionDao,
    context: Context
) {
    private val backupManager = BackupManager(context)
    private val preferencesManager = PreferencesManager(context)
    val allTransactions: LiveData<List<Transaction>> = transactionDao.getAllTransactions()

    suspend fun insert(transaction: Transaction) {
        transactionDao.insert(transaction)
    }

    suspend fun update(transaction: Transaction) {
        transactionDao.update(transaction)
    }

    suspend fun delete(transaction: Transaction) {
        transactionDao.delete(transaction)
    }

    fun getTransactionsByType(type: TransactionType): LiveData<List<Transaction>> {
        return transactionDao.getTransactionsByType(type)
    }

    fun getTransactionsBetweenDates(startDate: Date, endDate: Date): LiveData<List<Transaction>> {
        return transactionDao.getTransactionsBetweenDates(startDate, endDate)
    }

    fun getTotalByType(type: TransactionType): LiveData<Double?> {
        return transactionDao.getTotalByType(type)
    }

    fun getCategoryTotals(type: TransactionType): LiveData<List<CategoryTotal>> {
        return transactionDao.getCategoryTotals(type)
    }

    suspend fun exportToJson(): BackupManager.BackupResult = withContext(Dispatchers.IO) {
        // Get transactions directly from DAO instead of LiveData
        val transactions = transactionDao.getAllTransactionsSync()
        Log.d("Backup", "Exporting ${transactions.size} transactions")
        backupManager.createBackup(transactions)
    }

    suspend fun importFromJson(filePath: String): BackupManager.RestoreResult = withContext(Dispatchers.IO) {
        val result = backupManager.restoreFromFile(java.io.File(filePath))
        
        when (result) {
            is BackupManager.RestoreResult.Success -> {
                try {
                    Log.d("Restore", "Attempting to restore ${result.transactions.size} transactions")
                    // Clear existing transactions first
                    transactionDao.deleteAll()
                    Log.d("Restore", "Deleted existing transactions")
                    
                    // Insert all transactions in a single transaction block
                    transactionDao.insertAll(result.transactions)
                    Log.d("Restore", "Inserted restored transactions")
                    
                    // Restore preferences
                    preferencesManager.apply {
                        monthlyBudget = result.preferences.monthlyBudget
                        preferredCurrency = result.preferences.preferredCurrency
                        isNotificationsEnabled = result.preferences.isNotificationsEnabled
                    }
                    Log.d("Restore", "Restored preferences")
                } catch (e: Exception) {
                    Log.e("Restore", "Error during restore", e)
                    // If anything fails during restore, return error
                    return@withContext BackupManager.RestoreResult.Error(
                        "Failed to restore data: ${e.message}",
                        e
                    )
                }
            }
            is BackupManager.RestoreResult.Error -> {
                Log.e("Restore", "Restore error: ${result.message}")
            }
        }
        
        result
    }

    suspend fun getAvailableBackups() = withContext(Dispatchers.IO) {
        backupManager.getAvailableBackups()
    }
} 