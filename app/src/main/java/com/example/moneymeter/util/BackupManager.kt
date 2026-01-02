package com.example.moneymeter.util

import android.content.Context
import android.util.Log
import com.example.moneymeter.data.Transaction
import com.example.moneymeter.data.PreferencesManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*

class BackupManager(private val context: Context) {
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(Date::class.java, object : JsonSerializer<Date>, JsonDeserializer<Date> {
            override fun serialize(src: Date?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
                return JsonPrimitive(src?.time)
            }

            override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Date {
                return Date(json?.asLong ?: 0)
            }
        })
        .create()
        
    private val backupDir: File = File(context.getExternalFilesDir(null), "backups")
    private val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
    private val preferencesManager = PreferencesManager(context)

    init {
        if (!backupDir.exists()) {
            backupDir.mkdirs()
        }
    }

    data class BackupData(
        val transactions: List<Transaction>,
        val preferences: PreferencesData
    )

    data class PreferencesData(
        val monthlyBudget: Double,
        val preferredCurrency: String,
        val isNotificationsEnabled: Boolean
    )

    sealed class BackupResult {
        data class Success(val file: File) : BackupResult()
        data class Error(val message: String, val exception: Exception? = null) : BackupResult()
    }

    sealed class RestoreResult {
        data class Success(
            val transactions: List<Transaction>,
            val preferences: PreferencesData
        ) : RestoreResult()
        data class Error(val message: String, val exception: Exception? = null) : RestoreResult()
    }

    fun createBackup(transactions: List<Transaction>): BackupResult {
        return try {
            Log.d("Backup", "Creating backup with ${transactions.size} transactions")
            val backupData = BackupData(
                transactions = transactions,
                preferences = PreferencesData(
                    monthlyBudget = preferencesManager.monthlyBudget,
                    preferredCurrency = preferencesManager.preferredCurrency,
                    isNotificationsEnabled = preferencesManager.isNotificationsEnabled
                )
            )

            val backupFile = File(backupDir, "backup_${dateFormat.format(Date())}.json")
            FileWriter(backupFile).use { writer ->
                val json = gson.toJson(backupData)
                Log.d("Backup", "Backup JSON: $json")
                writer.write(json)
            }
            Log.d("Backup", "Backup file created at ${backupFile.absolutePath}")
            BackupResult.Success(backupFile)
        } catch (e: Exception) {
            Log.e("Backup", "Backup failed", e)
            BackupResult.Error("Failed to create backup: ${e.message}", e)
        }
    }

    fun restoreFromFile(file: File): RestoreResult {
        return try {
            Log.d("Restore", "Reading backup file: ${file.absolutePath}")
            val json = FileReader(file).use { it.readText() }
            Log.d("Restore", "Read JSON: $json")
            
            val backupData = gson.fromJson<BackupData>(json, object : TypeToken<BackupData>() {}.type)
            
            if (backupData == null) {
                Log.e("Restore", "Backup data is null")
                return RestoreResult.Error("Invalid backup file format")
            }

            Log.d("Restore", "Parsed ${backupData.transactions.size} transactions from backup")
            RestoreResult.Success(
                transactions = backupData.transactions,
                preferences = backupData.preferences
            )
        } catch (e: JsonSyntaxException) {
            Log.e("Restore", "JSON syntax error", e)
            RestoreResult.Error("Invalid backup file format: ${e.message}", e)
        } catch (e: IOException) {
            Log.e("Restore", "IO error", e)
            RestoreResult.Error("Failed to read backup file: ${e.message}", e)
        } catch (e: Exception) {
            Log.e("Restore", "Unexpected error", e)
            RestoreResult.Error("Unexpected error during restore: ${e.message}", e)
        }
    }

    fun getAvailableBackups(): List<File> {
        return backupDir.listFiles { file ->
            file.isFile && file.name.endsWith(".json")
        }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    companion object {
        private const val MAX_BACKUPS = 5
    }
} 