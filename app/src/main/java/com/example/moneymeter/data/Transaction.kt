package com.example.moneymeter.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val category: String,
    val date: Date,
    val type: TransactionType,
    val createdAt: Date = Date()
)

enum class TransactionType {
    INCOME, EXPENSE
}

enum class IncomeCategory {
    SALARY,
    BUSINESS,
    INVESTMENT,
    RENTAL,
    OTHER
}

enum class ExpenseCategory {
    FOOD,
    TRANSPORT,
    BILLS,
    ENTERTAINMENT,
    SHOPPING,
    HEALTH,
    EDUCATION,
    OTHER
} 