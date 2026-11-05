package com.dasuni.healthmate

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
class SummaryActivity : AppCompatActivity() {
    private lateinit var viewModel: RecordHistoryViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_summary)

        viewModel = androidx.lifecycle.ViewModelProvider(this)[
            RecordHistoryViewModel::class.java
        ]

        val summaryDate = findViewById<android.widget.TextView>(R.id.textSummaryDate)
        val summarySteps = findViewById<android.widget.TextView>(R.id.textSummarySteps)
        val summaryCalories = findViewById<android.widget.TextView>(R.id.textSummaryCalories)
        val summaryWater = findViewById<android.widget.TextView>(R.id.textSummaryWater)

        val waterGoalText = findViewById<android.widget.TextView>(R.id.textWaterGoal)
        val waterProgress = findViewById<android.widget.ProgressBar>(R.id.progressWater)
        val waterMessage = findViewById<android.widget.TextView>(R.id.textWaterMessage)

        val waterGoal = 3000L

        lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.historyState.collect { state ->
                    val today = java.time.LocalDate.now().toString()
                    summaryDate.text = "Today: $today"

                    when {
                        state.isLoading -> {
                            summarySteps.text = "Loading records..."
                            summaryCalories.text = ""
                            summaryWater.text = ""

                            waterGoalText.text = "Example water goal: $waterGoal ml"
                            waterProgress.progress = 0
                            waterMessage.text = ""
                            waterMessage.background = null
                            waterMessage.setPadding(0, 0, 0, 0)
                        }

                        state.error != null -> {
                            summarySteps.text = "Could not load records."
                            summaryCalories.text = ""
                            summaryWater.text = ""

                            waterGoalText.text = "Example water goal: $waterGoal ml"
                            waterProgress.progress = 0
                            waterMessage.text = ""
                            waterMessage.background = null
                            waterMessage.setPadding(0, 0, 0, 0)
                        }

                        else -> {
                            val todayRecords = state.records.filter { record ->
                                record.date == today
                            }

                            val totalSteps = todayRecords.sumOf { it.steps.toLong() }
                            val totalCalories = todayRecords.sumOf { it.calories.toLong() }
                            val totalWater = todayRecords.sumOf { it.water.toLong() }

                            summarySteps.text = "Steps: $totalSteps"
                            summaryCalories.text = "Calories: $totalCalories kcal"
                            summaryWater.text = "Water: $totalWater ml"

                            waterGoalText.text = "Your water goal: $totalWater / $waterGoal ml"

// The bar stops at 100%,  if the recorded amount exceeds the goal.
                            val progress = ((totalWater.coerceAtMost(waterGoal) * 100) / waterGoal).toInt()
                            waterProgress.progress = progress

                            if (totalWater >= waterGoal) {
                                waterMessage.text =
                                    "🏆 Goal reached!\n\nYou reached your example water goal today."

                                waterMessage.setBackgroundResource(R.drawable.menu_button_teal)

                                val padding = (20 * resources.displayMetrics.density).toInt()
                                waterMessage.setPadding(padding, padding, padding, padding)
                            } else {
                                waterMessage.text = "You have recorded $totalWater ml today."
                                waterMessage.background = null
                                waterMessage.setPadding(0, 0, 0, 0)
                            }
                        }
                    }
                }
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val spacing = (24 * resources.displayMetrics.density).toInt()
            v.setPadding(
                systemBars.left + spacing,
                systemBars.top + spacing,
                systemBars.right + spacing,
                systemBars.bottom + spacing
            )
            insets
        }

    }
    override fun onResume() {
        super.onResume()
        viewModel.loadRecords()
    }
}