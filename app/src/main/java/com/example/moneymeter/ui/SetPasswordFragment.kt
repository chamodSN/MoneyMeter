package com.example.moneymeter.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.example.moneymeter.R
import com.example.moneymeter.data.PreferencesManager
import com.example.moneymeter.util.ValidationUtil
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class SetPasswordFragment : Fragment() {
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var passwordInput: TextInputEditText
    private lateinit var confirmPasswordInput: TextInputEditText
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var confirmPasswordLayout: TextInputLayout
    private lateinit var saveButton: MaterialButton
    private lateinit var lockAnimation: LottieAnimationView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_set_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferencesManager = PreferencesManager(requireContext())
        
        // Initialize views
        passwordInput = view.findViewById(R.id.passwordInput)
        confirmPasswordInput = view.findViewById(R.id.confirmPasswordInput)
        passwordLayout = view.findViewById(R.id.passwordLayout)
        confirmPasswordLayout = view.findViewById(R.id.confirmPasswordLayout)
        saveButton = view.findViewById(R.id.saveButton)
        lockAnimation = view.findViewById(R.id.lockAnimation)

        // Set up animation
        lockAnimation.setAnimation(R.raw.lock)
        lockAnimation.playAnimation()

        saveButton.setOnClickListener {
            val password = passwordInput.text.toString()
            val confirmPassword = confirmPasswordInput.text.toString()

            val validation = ValidationUtil.validateNewPassword(password, confirmPassword)
            if (validation.isValid) {
                preferencesManager.password = password
                preferencesManager.hasSetPassword = true
                // Navigate to main activity or next onboarding step
                activity?.finish()
            } else {
                Toast.makeText(context, validation.errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }
} 