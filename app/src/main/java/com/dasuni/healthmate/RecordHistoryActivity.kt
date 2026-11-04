package com.dasuni.healthmate

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch

class RecordHistoryActivity : AppCompatActivity() {

    private lateinit var viewModel: RecordHistoryViewModel
    private var displayedRecords: List<HealthRecord> = emptyList()
    private var selectedDate: String? = null
    private var showRecords = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_record_history)

        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { view, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )
            view.setPadding(
                bars.left,
                bars.top,
                bars.right,
                bars.bottom
            )
            insets
        }

        viewModel = ViewModelProvider(this)[
            RecordHistoryViewModel::class.java
        ]

        val statusText =
            findViewById<TextView>(R.id.textHistoryStatus)
        val recordsList =
            findViewById<ListView>(R.id.listHealthRecords)

        val adapter = ArrayAdapter<String>(
            this,
            android.R.layout.simple_list_item_1,
            mutableListOf<String>()
        )
        recordsList.adapter = adapter
        findViewById<android.widget.Button>(R.id.buttonFilterDate)
            .setOnClickListener {
                val today = java.util.Calendar.getInstance()

                android.app.DatePickerDialog(
                    this,
                    { _, year, month, day ->
                        selectedDate = String.format(
                            java.util.Locale.US,
                            "%04d-%02d-%02d",
                            year,
                            month + 1,
                            day
                        )
                        showRecords = true
                        viewModel.loadRecords()
                    },
                    today.get(java.util.Calendar.YEAR),
                    today.get(java.util.Calendar.MONTH),
                    today.get(java.util.Calendar.DAY_OF_MONTH)
                ).show()
            }
        findViewById<android.widget.Button>(R.id.buttonShowAll)
            .setOnClickListener {
                selectedDate = null
                showRecords = true
                viewModel.loadRecords()
            }

        recordsList.setOnItemClickListener { _, _, position, _ ->
            val state = viewModel.historyState.value

            if (!state.isLoading && state.error == null) {
                val record = displayedRecords.getOrNull(position)

                if (record != null) {
                    val editIntent = android.content.Intent(
                        this,
                        AddRecordActivity::class.java
                    ).apply {
                        putExtra("record_id", record.id)
                        putExtra("record_date", record.date)
                        putExtra("record_steps", record.steps)
                        putExtra("record_calories", record.calories)
                        putExtra("record_water", record.water)
                    }

                    startActivity(editIntent)
                }
            }
        }

        recordsList.setOnItemLongClickListener { _, _, position, _ ->
            val state = viewModel.historyState.value

            if (state.isLoading) {
                return@setOnItemLongClickListener true
            }

            val record = displayedRecords.getOrNull(position)
                ?: return@setOnItemLongClickListener true

            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete record?")
                .setMessage(
                    "Date: ${record.date}\n" +
                            "Steps: ${record.steps}\n" +
                            "Calories: ${record.calories} kcal\n" +
                            "Water: ${record.water} ml\n\n" +
                            "This record will be permanently deleted."
                )
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete") { _, _ ->
                    viewModel.deleteRecord(record.id)
                }
                .show()

            true
        }

        // Tap an error message to retry loading.
        statusText.setOnClickListener {
            if (viewModel.historyState.value.error != null) {
                viewModel.loadRecords()
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.historyState.collect { state ->
                    val message = when {
                        state.isLoading -> "Loading records..."
                        state.error != null ->
                            "${state.error} Tap here to retry."
                        state.records.isEmpty() ->
                            "No records yet. Add your first record."
                        selectedDate != null &&
                                state.records.none { record -> record.date == selectedDate } ->
                            "No records found for $selectedDate."
                        else -> null
                    }

                    statusText.text = message.orEmpty()
                    statusText.visibility =
                        if (message == null) View.GONE else View.VISIBLE

                    displayedRecords = if (!showRecords) {
                        emptyList()
                    } else if (selectedDate == null) {
                        state.records
                    } else {
                        state.records.filter { record -> record.date == selectedDate }
                    }
                    adapter.clear()
                    adapter.addAll(
                        displayedRecords.map { record ->
                            "${record.date}\n" +
                                    "Steps: ${record.steps}\n" +
                                    "Calories: ${record.calories} kcal\n" +
                                    "Water: ${record.water} ml"
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadRecords()
    }
}