package com.example.moneymeter.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moneymeter.R
import com.example.moneymeter.data.ExpenseCategory
import com.example.moneymeter.data.IncomeCategory
import com.example.moneymeter.data.Transaction
import com.example.moneymeter.data.TransactionType
import com.example.moneymeter.viewmodel.TransactionViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import java.util.Date

class TransactionsFragment : Fragment() {
    private val viewModel: TransactionViewModel by viewModels()
    private lateinit var transactionAdapter: TransactionAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_transactions, container, false)
        
        // Set up RecyclerView
        val recyclerView = view.findViewById<RecyclerView>(R.id.transactionsRecyclerView)
        transactionAdapter = TransactionAdapter { transaction ->
            showEditTransactionDialog(transaction)
        }
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = transactionAdapter
        }

        // Observe transactions
        viewModel.allTransactions.observe(viewLifecycleOwner) { transactions ->
            transactionAdapter.submitList(transactions)
        }
        
        // Set up FAB
        view.findViewById<FloatingActionButton>(R.id.fabAddTransaction).setOnClickListener {
            showAddTransactionDialog()
        }
        
        return view
    }

    private fun showAddTransactionDialog() {
        context?.let { ctx ->
            val dialog = BottomSheetDialog(ctx)
            val dialogView = layoutInflater.inflate(R.layout.dialog_add_transaction, null)
            dialog.setContentView(dialogView)

            // Get references to views
            val categoryDropdown = dialogView.findViewById<AutoCompleteTextView>(R.id.categoryDropdown)
            val typeRadioGroup = dialogView.findViewById<RadioGroup>(R.id.typeRadioGroup)

            // Initialize with expense categories (default selection)
            val expenseCategories = ExpenseCategory.values().map { it.name.capitalize() }
            var categoryAdapter = ArrayAdapter(ctx, android.R.layout.simple_dropdown_item_1line, expenseCategories)
            categoryDropdown.setAdapter(categoryAdapter)

            // Handle radio button changes
            typeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
                val categories = when (checkedId) {
                    R.id.expenseRadio -> ExpenseCategory.values().map { it.name.capitalize() }
                    R.id.incomeRadio -> IncomeCategory.values().map { it.name.capitalize() }
                    else -> emptyList()
                }
                categoryAdapter = ArrayAdapter(ctx, android.R.layout.simple_dropdown_item_1line, categories)
                categoryDropdown.setAdapter(categoryAdapter)
                categoryDropdown.text.clear() // Clear the current selection
            }

            dialogView.findViewById<View>(R.id.cancelButton).setOnClickListener {
                dialog.dismiss()
            }

            dialogView.findViewById<View>(R.id.saveButton).setOnClickListener {
                val amount = dialogView.findViewById<TextInputEditText>(R.id.amountInput).text.toString()
                val description = dialogView.findViewById<TextInputEditText>(R.id.descriptionInput).text.toString()
                val category = categoryDropdown.text.toString()

                if (amount.isBlank() || description.isBlank() || category.isBlank()) {
                    Toast.makeText(ctx, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                try {
                    val transaction = Transaction(
                        title = description,
                        amount = amount.toDouble(),
                        category = category,
                        date = Date(),
                        type = if (typeRadioGroup.checkedRadioButtonId == R.id.expenseRadio) 
                            TransactionType.EXPENSE else TransactionType.INCOME
                    )
                    viewModel.insert(transaction)
                    Toast.makeText(ctx, "Transaction saved", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                } catch (e: NumberFormatException) {
                    Toast.makeText(ctx, "Invalid amount format", Toast.LENGTH_SHORT).show()
                }
            }

            dialog.show()
        }
    }

    private fun showEditTransactionDialog(transaction: Transaction) {
        context?.let { ctx ->
            val dialog = BottomSheetDialog(ctx)
            val dialogView = layoutInflater.inflate(R.layout.dialog_edit_transaction, null)
            dialog.setContentView(dialogView)

            // Get references to views
            val amountInput = dialogView.findViewById<TextInputEditText>(R.id.amountInput)
            val descriptionInput = dialogView.findViewById<TextInputEditText>(R.id.descriptionInput)
            val categoryDropdown = dialogView.findViewById<AutoCompleteTextView>(R.id.categoryDropdown)
            val typeRadioGroup = dialogView.findViewById<RadioGroup>(R.id.typeRadioGroup)

            // Set current values
            amountInput.setText(transaction.amount.toString())
            descriptionInput.setText(transaction.title)
            
            // Set transaction type
            when (transaction.type) {
                TransactionType.EXPENSE -> typeRadioGroup.check(R.id.expenseRadio)
                TransactionType.INCOME -> typeRadioGroup.check(R.id.incomeRadio)
            }

            // Set up category adapter based on type
            val categories = when (transaction.type) {
                TransactionType.EXPENSE -> ExpenseCategory.values().map { it.name.capitalize() }
                TransactionType.INCOME -> IncomeCategory.values().map { it.name.capitalize() }
            }
            var categoryAdapter = ArrayAdapter(ctx, android.R.layout.simple_dropdown_item_1line, categories)
            categoryDropdown.setAdapter(categoryAdapter)
            categoryDropdown.setText(transaction.category)

            // Handle radio button changes
            typeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
                val newCategories = when (checkedId) {
                    R.id.expenseRadio -> ExpenseCategory.values().map { it.name.capitalize() }
                    R.id.incomeRadio -> IncomeCategory.values().map { it.name.capitalize() }
                    else -> emptyList()
                }
                categoryAdapter = ArrayAdapter(ctx, android.R.layout.simple_dropdown_item_1line, newCategories)
                categoryDropdown.setAdapter(categoryAdapter)
                categoryDropdown.text.clear()
            }

            // Handle delete button
            dialogView.findViewById<View>(R.id.deleteButton).setOnClickListener {
                AlertDialog.Builder(ctx)
                    .setTitle("Delete Transaction")
                    .setMessage("Are you sure you want to delete this transaction?")
                    .setPositiveButton("Delete") { _, _ ->
                        viewModel.delete(transaction)
                        Toast.makeText(ctx, "Transaction deleted", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }

            // Handle cancel button
            dialogView.findViewById<View>(R.id.cancelButton).setOnClickListener {
                dialog.dismiss()
            }

            // Handle update button
            dialogView.findViewById<View>(R.id.updateButton).setOnClickListener {
                val amount = amountInput.text.toString()
                val description = descriptionInput.text.toString()
                val category = categoryDropdown.text.toString()

                if (amount.isBlank() || description.isBlank() || category.isBlank()) {
                    Toast.makeText(ctx, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                try {
                    val updatedTransaction = transaction.copy(
                        title = description,
                        amount = amount.toDouble(),
                        category = category,
                        type = if (typeRadioGroup.checkedRadioButtonId == R.id.expenseRadio) 
                            TransactionType.EXPENSE else TransactionType.INCOME
                    )
                    viewModel.update(updatedTransaction)
                    Toast.makeText(ctx, "Transaction updated", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                } catch (e: NumberFormatException) {
                    Toast.makeText(ctx, "Invalid amount format", Toast.LENGTH_SHORT).show()
                }
            }

            dialog.show()
        }
    }

    private fun String.capitalize() = this.lowercase().replaceFirstChar { it.uppercase() }
}