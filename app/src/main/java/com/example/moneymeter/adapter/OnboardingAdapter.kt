package com.example.moneymeter.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.moneymeter.R
import com.example.moneymeter.model.OnboardingItem

class OnboardingAdapter(private val onboardingItems: List<OnboardingItem>) :
    RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder>() {

    inner class OnboardingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textTitle = view.findViewById<TextView>(R.id.textTitle)
        private val textDescription = view.findViewById<TextView>(R.id.textDescription)
        private val animationView = view.findViewById<LottieAnimationView>(R.id.animationView)

        fun bind(onboardingItem: OnboardingItem) {
            textTitle.text = onboardingItem.title
            textDescription.text = onboardingItem.description
            
            try {
                animationView.setAnimation(onboardingItem.animationResId)
                animationView.playAnimation()
            } catch (e: Exception) {
                // If animation fails to load, we'll just hide the animation view
                animationView.visibility = View.GONE
                e.printStackTrace()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        return OnboardingViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_onboarding,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        holder.bind(onboardingItems[position])
    }

    override fun getItemCount() = onboardingItems.size
} 