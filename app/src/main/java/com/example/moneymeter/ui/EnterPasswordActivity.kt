package com.example.moneymeter.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.example.moneymeter.ui.MainActivity
import com.example.moneymeter.R
import com.example.moneymeter.data.PreferencesManager
import com.example.moneymeter.util.ValidationUtil
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class EnterPasswordActivity : AppCompatActivity() {
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var passwordInput: TextInputEditText
    private lateinit var confirmPasswordInput: TextInputEditText
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var confirmPasswordLayout: TextInputLayout
    private lateinit var actionButton: MaterialButton
    private lateinit var lockAnimation: LottieAnimationView
    private var isSetupMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter_password)

        preferencesManager = PreferencesManager(this)
        isSetupMode = !preferencesManager.hasSetPassword

        // Initialize views
        passwordInput = findViewById(R.id.passwordInput)
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput)
        passwordLayout = findViewById(R.id.passwordLayout)
        confirmPasswordLayout = findViewById(R.id.confirmPasswordLayout)
        actionButton = findViewById(R.id.verifyButton)
        lockAnimation = findViewById(R.id.lockAnimation)

        // Set up UI based on mode
        setupUI()

        // Set up animation
        lockAnimation.setAnimation(R.raw.lock)
        lockAnimation.playAnimation()

        actionButton.setOnClickListener {
            if (isSetupMode) {
                handlePasswordSetup()
            } else {
                handlePasswordVerification()
            }
        }
    }

    private fun setupUI() {
        if (isSetupMode) {
            passwordLayout.hint = "Enter new PIN"
            confirmPasswordLayout.visibility = android.view.View.VISIBLE
            actionButton.text = "Set PIN"
        } else {
            passwordLayout.hint = "Enter PIN"
            confirmPasswordLayout.visibility = android.view.View.GONE
            actionButton.text = "Verify"
        }
    }

    private fun handlePasswordSetup() {
        val newPassword = passwordInput.text.toString()
        val confirmPassword = confirmPasswordInput.text.toString()

        val validation = ValidationUtil.validateNewPassword(newPassword, confirmPassword)
        if (validation.isValid) {
            preferencesManager.apply {
                password = newPassword
                hasSetPassword = true
            }
            startMainActivity()
        } else {
            Toast.makeText(this, validation.errorMessage, Toast.LENGTH_SHORT).show()
            passwordInput.text?.clear()
            confirmPasswordInput.text?.clear()
        }
    }

    private fun handlePasswordVerification() {
        val enteredPassword = passwordInput.text.toString()
        val storedPassword = preferencesManager.password

        val validation = ValidationUtil.validateCurrentPassword(enteredPassword, storedPassword)
        if (validation.isValid) {
            startMainActivity()
        } else {
            Toast.makeText(this, validation.errorMessage, Toast.LENGTH_SHORT).show()
            passwordInput.text?.clear()
        }
    }

    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
} 