package com.example.moneymeter.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.example.moneymeter.R
import com.example.moneymeter.data.PreferencesManager

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var splashAnimation: LottieAnimationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        
        preferencesManager = PreferencesManager(this)
        splashAnimation = findViewById(R.id.splashAnimation)
        
        // Set up animation
        splashAnimation.apply {
            imageAssetsFolder = "raw/"
            setAnimation(R.raw.animation)
            speed = 1.0f  // Normal speed
            repeatCount = 0  // Play once
            playAnimation()
        }

        // Add animation completion listener
        splashAnimation.addAnimatorUpdateListener { valueAnimator ->
            // Check if animation is at the end
            if (valueAnimator.animatedFraction >= 0.99f && !isFinishing) {
                // Remove listener to prevent multiple calls
                splashAnimation.removeAllAnimatorListeners()
                splashAnimation.removeAllUpdateListeners()
                // Navigate to next screen
                navigateToNextScreen()
            }
        }
    }

    private fun navigateToNextScreen() {
        try {
            val intent = when {
                preferencesManager.isFirstLaunch -> Intent(this, OnboardingActivity::class.java)
                preferencesManager.hasSetPassword -> Intent(this, EnterPasswordActivity::class.java)
                else -> Intent(this, MainActivity::class.java)
            }
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to MainActivity if there's any issue
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}