package com.example.moneymeter.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moneymeter.R
import com.example.moneymeter.data.CategoryTotal
import com.example.moneymeter.data.TransactionType
import com.example.moneymeter.data.PreferencesManager
import com.example.moneymeter.viewmodel.TransactionViewModel
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import java.text.NumberFormat
import java.util.Locale
import com.example.moneymeter.util.CurrencyUtil
import kotlin.math.abs

class AnalyticsFragment : Fragment() {
    private val viewModel: TransactionViewModel by viewModels()
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var expensePieChart: PieChart
    private lateinit var incomePieChart: PieChart
    private lateinit var monthlySpendingText: TextView
    private lateinit var monthlyIncomeText: TextView
    private lateinit var expenseCategoryRecyclerView: RecyclerView
    private lateinit var incomeCategoryRecyclerView: RecyclerView
    private lateinit var expenseCategoryAdapter: CategoryTotalAdapter
    private lateinit var incomeCategoryAdapter: CategoryTotalAdapter
    private lateinit var budgetProgressBar: ProgressBar
    private lateinit var budgetProgressText: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_analytics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        preferencesManager = PreferencesManager(requireContext())
        
        // Initialize views
        expensePieChart = view.findViewById(R.id.expensePieChart)
        incomePieChart = view.findViewById(R.id.incomePieChart)
        monthlySpendingText = view.findViewById(R.id.monthlySpendingText)
        monthlyIncomeText = view.findViewById(R.id.monthlyIncomeText)
        expenseCategoryRecyclerView = view.findViewById(R.id.expenseCategoryRecyclerView)
        incomeCategoryRecyclerView = view.findViewById(R.id.incomeCategoryRecyclerView)
        budgetProgressBar = view.findViewById(R.id.budgetProgressBar)
        budgetProgressText = view.findViewById(R.id.budgetProgressText)

        setupPieCharts()
        setupRecyclerViews()
        observeData()
    }

    private fun setupPieCharts() {
        setupPieChart(expensePieChart)
        setupPieChart(incomePieChart)
    }

    private fun setupPieChart(pieChart: PieChart) {
        pieChart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            setTransparentCircleColor(Color.WHITE)
            setTransparentCircleAlpha(110)
            holeRadius = 58f
            transparentCircleRadius = 61f
            setDrawCenterText(true)
            rotationAngle = 0f
            isRotationEnabled = true
            isHighlightPerTapEnabled = true
            setUsePercentValues(true)
            legend.isEnabled = true
        }
    }

    private fun setupRecyclerViews() {
        expenseCategoryAdapter = CategoryTotalAdapter()
        incomeCategoryAdapter = CategoryTotalAdapter()

        expenseCategoryRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = expenseCategoryAdapter
        }

        incomeCategoryRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = incomeCategoryAdapter
        }
    }

    private fun updateBudgetProgress(totalExpenses: Double) {
        val monthlyBudget = preferencesManager.monthlyBudget
        if (monthlyBudget > 0) {
            val progress = ((totalExpenses / monthlyBudget) * 100).toInt().coerceIn(0, 100)
            budgetProgressBar.progress = progress
            
            val remaining = monthlyBudget - totalExpenses
            val currencyCode = preferencesManager.preferredCurrency
            val formattedRemaining = CurrencyUtil.formatAmount(abs(remaining), currencyCode)
            val formattedBudget = CurrencyUtil.formatAmount(monthlyBudget, currencyCode)
            
            budgetProgressText.text = when {
                remaining < 0 -> "Over budget by $formattedRemaining"
                remaining > 0 -> "$formattedRemaining remaining of $formattedBudget budget"
                else -> "Budget fully spent"
            }
            
            // Change progress bar color based on progress
            val colorRes = when {
                progress >= 90 -> R.color.expense_red
                progress >= 75 -> android.R.color.holo_orange_dark
                else -> R.color.income_green
            }
            budgetProgressBar.progressTintList = ContextCompat.getColorStateList(requireContext(), colorRes)
        } else {
            budgetProgressText.text = "Set a monthly budget in Settings"
            budgetProgressBar.progress = 0
        }
    }

    private fun observeData() {
        // Observe expenses
        viewModel.getCategoryTotals(TransactionType.EXPENSE).observe(viewLifecycleOwner) { expenseTotals ->
            updatePieChart(expensePieChart, expenseTotals, TransactionType.EXPENSE)
            expenseCategoryAdapter.submitList(expenseTotals)
            
            val totalExpense = expenseTotals.sumOf { it.amount }
            val currencyCode = preferencesManager.preferredCurrency
            val formattedExpense = CurrencyUtil.formatAmount(totalExpense, currencyCode)
            monthlySpendingText.text = "Total Expenses: $formattedExpense"
            
            updateBudgetProgress(totalExpense)
        }

        // Observe income
        viewModel.getCategoryTotals(TransactionType.INCOME).observe(viewLifecycleOwner) { incomeTotals ->
            updatePieChart(incomePieChart, incomeTotals, TransactionType.INCOME)
            incomeCategoryAdapter.submitList(incomeTotals)
            
            val totalIncome = incomeTotals.sumOf { it.amount }
            val currencyCode = preferencesManager.preferredCurrency
            val formattedIncome = CurrencyUtil.formatAmount(totalIncome, currencyCode)
            monthlyIncomeText.text = "Total Income: $formattedIncome"
        }
    }

    private fun updatePieChart(pieChart: PieChart, categoryTotals: List<CategoryTotal>, type: TransactionType) {
        if (categoryTotals.isEmpty()) {
            pieChart.setNoDataText("No ${type.name.lowercase()} transactions")
            pieChart.invalidate()
            return
        }

        val entries = categoryTotals.map { categoryTotal ->
            PieEntry(categoryTotal.amount.toFloat(), categoryTotal.category)
        }

        val colors = categoryTotals.mapIndexed { index, _ ->
            val baseColor = when (type) {
                TransactionType.EXPENSE -> ContextCompat.getColor(requireContext(), R.color.expense_red)
                TransactionType.INCOME -> ContextCompat.getColor(requireContext(), R.color.income_green)
            }
            adjustColorShade(baseColor, index, categoryTotals.size)
        }

        val dataSet = PieDataSet(entries, if (type == TransactionType.EXPENSE) "Expenses" else "Income").apply {
            this.colors = colors
            valueTextSize = 12f
            valueTextColor = Color.WHITE
        }

        val pieData = PieData(dataSet).apply {
            setValueFormatter(PercentFormatter(pieChart))
        }

        pieChart.data = pieData
        pieChart.invalidate()
    }

    private fun adjustColorShade(baseColor: Int, index: Int, total: Int): Int {
        if (total <= 1) return baseColor
        
        val factor = 0.8f + (0.4f * index / (total - 1))
        val red = Color.red(baseColor)
        val green = Color.green(baseColor)
        val blue = Color.blue(baseColor)
        
        return Color.rgb(
            (red * factor).coerceIn(0f, 255f).toInt(),
            (green * factor).coerceIn(0f, 255f).toInt(),
            (blue * factor).coerceIn(0f, 255f).toInt()
        )
    }
} 