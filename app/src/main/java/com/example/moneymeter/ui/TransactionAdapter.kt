package com.example.moneymeter.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.moneymeter.R
import com.example.moneymeter.data.PreferencesManager
import com.example.moneymeter.data.Transaction
import com.example.moneymeter.data.TransactionType
import com.example.moneymeter.util.CurrencyUtil
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionAdapter(
    private val onTransactionClick: (Transaction) -> Unit
) : ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder>(TransactionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view, onTransactionClick)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TransactionViewHolder(
        itemView: View,
        private val onTransactionClick: (Transaction) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val titleText: TextView = itemView.findViewById(R.id.transactionTitle)
        private val amountText: TextView = itemView.findViewById(R.id.transactionAmount)
        private val categoryText: TextView = itemView.findViewById(R.id.transactionCategory)
        private val dateText: TextView = itemView.findViewById(R.id.transactionDate)
        private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        private val preferencesManager = PreferencesManager(itemView.context)

        fun bind(transaction: Transaction) {
            titleText.text = transaction.title
            categoryText.text = transaction.category
            dateText.text = dateFormat.format(transaction.date)

            // Set color based on transaction type
            val color = when (transaction.type) {
                TransactionType.INCOME -> R.color.income_green
                TransactionType.EXPENSE -> R.color.expense_red
            }
            amountText.setTextColor(ContextCompat.getColor(itemView.context, color))
            
            // Format amount with proper currency and sign
            val formattedAmount = CurrencyUtil.formatAmount(
                transaction.amount,
                preferencesManager.preferredCurrency
            )
            
            // Add sign based on type
            amountText.text = when (transaction.type) {
                TransactionType.INCOME -> "+ $formattedAmount"
                TransactionType.EXPENSE -> "- $formattedAmount"
            }

            // Set click listener
            itemView.setOnClickListener {
                onTransactionClick(transaction)
            }
        }
    }

    class TransactionDiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem == newItem
        }
    }
} 