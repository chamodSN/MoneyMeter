package com.example.moneymeter.ui

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.moneymeter.R
import com.example.moneymeter.adapter.OnboardingAdapter
import com.example.moneymeter.data.PreferencesManager
import com.example.moneymeter.model.OnboardingItem

class OnboardingActivity : AppCompatActivity() {
    private lateinit var onboardingAdapter: OnboardingAdapter
    private lateinit var indicatorLayout: LinearLayout
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        preferencesManager = PreferencesManager(this)

        if (!preferencesManager.isFirstLaunch) {
            startMainActivity()
            return
        }

        setupOnboardingItems()
        setupIndicators()
        setCurrentIndicator(0)
    }

    private fun setupOnboardingItems() {
        onboardingAdapter = OnboardingAdapter(
            listOf(
                OnboardingItem(
                    "Track Your Expenses",
                    "Keep track of your daily expenses and income with ease",
                    R.raw.onboardingscreen1
                ),
                OnboardingItem(
                    "Set Budgets",
                    "Set monthly budgets and get alerts when you're close to your limit",
                    R.raw.onboardingscreen2
                ),
                OnboardingItem(
                    "Analyze Your Spending",
                    "View detailed analytics of your spending habits",
                    R.raw.onboardingscreen3
                )
            )
        )

        val onboardingViewPager = findViewById<ViewPager2>(R.id.onboardingViewPager)
        onboardingViewPager.adapter = onboardingAdapter
        onboardingViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setCurrentIndicator(position)
            }
        })

        findViewById<Button>(R.id.buttonGetStarted).setOnClickListener {
            preferencesManager.isFirstLaunch = false
            startMainActivity()
        }
    }

    private fun setupIndicators() {
        indicatorLayout = findViewById(R.id.indicatorLayout)
        val indicators = arrayOfNulls<ImageView>(onboardingAdapter.itemCount)
        val layoutParams: LinearLayout.LayoutParams =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        layoutParams.setMargins(8, 0, 8, 0)
        for (i in indicators.indices) {
            indicators[i] = ImageView(applicationContext)
            indicators[i]?.let {
                it.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.indicator_inactive
                    )
                )
                it.layoutParams = layoutParams
                indicatorLayout.addView(it)
            }
        }
    }

    private fun setCurrentIndicator(position: Int) {
        val childCount = indicatorLayout.childCount
        for (i in 0 until childCount) {
            val imageView = indicatorLayout.getChildAt(i) as ImageView
            if (i == position) {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.indicator_active
                    )
                )
            } else {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.indicator_inactive
                    )
                )
            }
        }
    }

    private fun startMainActivity() {
        if (!preferencesManager.hasSetPassword) {
            // If password is not set, go to password setup
            startActivity(Intent(this, EnterPasswordActivity::class.java))
        } else {
            // If password is already set, go to main activity
            startActivity(Intent(this, MainActivity::class.java))
        }
        finish()
    }
} 