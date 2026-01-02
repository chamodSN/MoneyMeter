package com.example.moneymeter.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.moneymeter.R
import com.example.moneymeter.data.PreferencesManager
import com.example.moneymeter.util.BackupManager
import com.example.moneymeter.util.CurrencyUtil
import com.example.moneymeter.util.ValidationUtil
import com.example.moneymeter.viewmodel.TransactionViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SettingsFragment : Fragment() {
    private val viewModel: TransactionViewModel by viewModels()
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var monthlyBudgetInput: TextInputEditText
    private lateinit var saveBudgetButton: MaterialButton
    private lateinit var currencyDropdown: AutoCompleteTextView
    private lateinit var saveCurrencyButton: MaterialButton
    private lateinit var backupButton: MaterialButton
    private lateinit var restoreButton: MaterialButton
    private lateinit var progressIndicator: LinearProgressIndicator

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        preferencesManager = PreferencesManager(requireContext())
        
        // Initialize views
        monthlyBudgetInput = view.findViewById(R.id.monthlyBudgetInput)
        saveBudgetButton = view.findViewById(R.id.saveBudgetButton)
        currencyDropdown = view.findViewById(R.id.currencyDropdown)
        saveCurrencyButton = view.findViewById(R.id.saveCurrencyButton)
        backupButton = view.findViewById(R.id.backupButton)
        restoreButton = view.findViewById(R.id.restoreButton)
        progressIndicator = view.findViewById(R.id.progressIndicator)

        setupCurrencyDropdown()
        setupBudgetInput()
        setupBackupRestore()
        setupPasswordChange()
    }

    private fun setupBudgetInput() {
        monthlyBudgetInput.setText(preferencesManager.monthlyBudget.toString())

        saveBudgetButton.setOnClickListener {
            val budgetText = monthlyBudgetInput.text.toString()
            if (budgetText.isNotEmpty()) {
                try {
                    val budget = budgetText.toDouble()
                    if (budget <= 0) {
                        Toast.makeText(context, "Budget must be greater than 0", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    preferencesManager.monthlyBudget = budget
                    Toast.makeText(context, "Budget saved successfully!", Toast.LENGTH_SHORT).show()
                } catch (e: NumberFormatException) {
                    Toast.makeText(context, "Please enter a valid number", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Please enter a budget amount", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupCurrencyDropdown() {
        val currencies = CurrencyUtil.availableCurrencies.map { 
            CurrencyUtil.getCurrencyDisplayName(it)
        }
        
        android.util.Log.d("SettingsFragment", "Available currencies: $currencies")
        
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            currencies
        )
        
        currencyDropdown.setAdapter(adapter)
        
        val currentCurrency = CurrencyUtil.getCurrencyByCode(preferencesManager.preferredCurrency)
        android.util.Log.d("SettingsFragment", "Current currency: ${currentCurrency.code}")
        currencyDropdown.setText(CurrencyUtil.getCurrencyDisplayName(currentCurrency), false)

        saveCurrencyButton.setOnClickListener {
            val selectedCurrency = currencyDropdown.text.toString()
            android.util.Log.d("SettingsFragment", "Selected currency: $selectedCurrency")
            val currencyCode = CurrencyUtil.availableCurrencies
                .find { CurrencyUtil.getCurrencyDisplayName(it) == selectedCurrency }
                ?.code ?: "USD"
            
            preferencesManager.preferredCurrency = currencyCode
            Toast.makeText(context, "Currency saved successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupBackupRestore() {
        backupButton.setOnClickListener {
            showBackupDialog()
        }

        restoreButton.setOnClickListener {
            showRestoreDialog()
        }
    }

    private fun setupPasswordChange() {
        view?.findViewById<MaterialButton>(R.id.changePasswordButton)?.setOnClickListener {
            showChangePasswordDialog()
        }
    }

    private fun showBackupDialog() {
        lifecycleScope.launch {
            progressIndicator.visibility = View.VISIBLE
            backupButton.isEnabled = false
            restoreButton.isEnabled = false

            when (val result = viewModel.exportData()) {
                is BackupManager.BackupResult.Success -> {
                    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                    val date = dateFormat.format(Date())
                    showSuccessDialog(
                        "Backup Successful",
                        "Your data has been backed up successfully.\nBackup created on: $date"
                    )
                    preferencesManager.lastBackupDate = System.currentTimeMillis()
                }
                is BackupManager.BackupResult.Error -> {
                    showErrorDialog(
                        "Backup Failed",
                        "Failed to create backup: ${result.message}"
                    )
                }
            }

            progressIndicator.visibility = View.GONE
            backupButton.isEnabled = true
            restoreButton.isEnabled = true
        }
    }

    private fun showRestoreDialog() {
        lifecycleScope.launch {
            val backups = viewModel.getAvailableBackups()
            if (backups.isEmpty()) {
                showErrorDialog("No Backups", "No backup files found")
                return@launch
            }

            val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            val items = backups.map { file ->
                dateFormat.format(Date(file.lastModified()))
            }.toTypedArray()

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Select Backup to Restore")
                .setItems(items) { dialog, which ->
                    dialog.dismiss()
                    performRestore(backups[which])
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun performRestore(backupFile: java.io.File) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Confirm Restore")
            .setMessage("This will replace all current data with the backup data. Are you sure you want to continue?")
            .setPositiveButton("Restore") { _, _ ->
                lifecycleScope.launch {
                    progressIndicator.visibility = View.VISIBLE
                    backupButton.isEnabled = false
                    restoreButton.isEnabled = false

                    when (val result = viewModel.importData(backupFile.absolutePath)) {
                        is BackupManager.RestoreResult.Success -> {
                            showSuccessDialog(
                                "Restore Successful",
                                "Your data has been restored successfully from the backup."
                            )
                            // Refresh UI with restored preferences
                            monthlyBudgetInput.setText(preferencesManager.monthlyBudget.toString())
                            val currentCurrency = CurrencyUtil.getCurrencyByCode(preferencesManager.preferredCurrency)
                            currencyDropdown.setText(CurrencyUtil.getCurrencyDisplayName(currentCurrency), false)
                        }
                        is BackupManager.RestoreResult.Error -> {
                            showErrorDialog(
                                "Restore Failed",
                                "Failed to restore data: ${result.message}"
                            )
                        }
                    }

                    progressIndicator.visibility = View.GONE
                    backupButton.isEnabled = true
                    restoreButton.isEnabled = true
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showSuccessDialog(title: String, message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showErrorDialog(title: String, message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showChangePasswordDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_change_password, null)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Change PIN")
            .setView(dialogView)
            .create()

        val currentPasswordInput = dialogView.findViewById<TextInputEditText>(R.id.currentPasswordInput)
        val newPasswordInput = dialogView.findViewById<TextInputEditText>(R.id.newPasswordInput)
        val confirmPasswordInput = dialogView.findViewById<TextInputEditText>(R.id.confirmPasswordInput)
        val updateButton = dialogView.findViewById<MaterialButton>(R.id.updateButton)
        val cancelButton = dialogView.findViewById<MaterialButton>(R.id.cancelButton)

        updateButton?.setOnClickListener {
            val currentPassword = currentPasswordInput?.text.toString()
            val newPassword = newPasswordInput?.text.toString()
            val confirmPassword = confirmPasswordInput?.text.toString()

            val currentValidation = ValidationUtil.validateCurrentPassword(
                currentPassword,
                preferencesManager.password
            )

            if (!currentValidation.isValid) {
                Toast.makeText(context, currentValidation.errorMessage, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newValidation = ValidationUtil.validateNewPassword(newPassword, confirmPassword)
            if (newValidation.isValid) {
                preferencesManager.password = newPassword
                Toast.makeText(context, "PIN updated successfully", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else {
                Toast.makeText(context, newValidation.errorMessage, Toast.LENGTH_SHORT).show()
            }
        }

        cancelButton?.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
} 