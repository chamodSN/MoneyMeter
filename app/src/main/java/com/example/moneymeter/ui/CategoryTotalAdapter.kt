package com.example.moneymeter.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.moneymeter.R
import com.example.moneymeter.data.CategoryTotal
import com.example.moneymeter.data.PreferencesManager
import com.example.moneymeter.util.CurrencyUtil

class CategoryTotalAdapter : ListAdapter<CategoryTotal, CategoryTotalAdapter.CategoryTotalViewHolder>(CategoryTotalDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryTotalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_total, parent, false)
        return CategoryTotalViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryTotalViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CategoryTotalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryText: TextView = itemView.findViewById(R.id.categoryName)
        private val amountText: TextView = itemView.findViewById(R.id.categoryAmount)
        private val preferencesManager = PreferencesManager(itemView.context)

        fun bind(categoryTotal: CategoryTotal) {
            categoryText.text = categoryTotal.category
            amountText.text = CurrencyUtil.formatAmount(
                categoryTotal.amount,
                preferencesManager.preferredCurrency
            )
        }
    }

    class CategoryTotalDiffCallback : DiffUtil.ItemCallback<CategoryTotal>() {
        override fun areItemsTheSame(oldItem: CategoryTotal, newItem: CategoryTotal): Boolean {
            return oldItem.category == newItem.category
        }

        override fun areContentsTheSame(oldItem: CategoryTotal, newItem: CategoryTotal): Boolean {
            return oldItem == newItem
        }
    }
} 