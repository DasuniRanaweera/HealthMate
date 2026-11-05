package com.dasuni.healthmate
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AddRecordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_record)
        val viewModel =
            androidx.lifecycle.ViewModelProvider(this)[
                AddRecordViewModel::class.java
            ]
        val dateButton =
            findViewById<android.widget.Button>(R.id.buttonSelectDate)

        val selectedDate = java.util.Calendar.getInstance()

        dateButton.setOnClickListener {
            val picker = android.app.DatePickerDialog(
                this,
                { _, year, month, day ->
                    selectedDate.set(year, month, day)

                    val formattedDate = String.format(
                        java.util.Locale.US,
                        "%04d-%02d-%02d",
                        year,
                        month + 1,
                        day
                    )

                    dateButton.text = formattedDate
                },
                selectedDate.get(java.util.Calendar.YEAR),
                selectedDate.get(java.util.Calendar.MONTH),
                selectedDate.get(java.util.Calendar.DAY_OF_MONTH)
            )

            picker.datePicker.maxDate = System.currentTimeMillis()
            picker.show()
        }

        val stepsInput =
            findViewById<android.widget.EditText>(R.id.inputSteps)
        val caloriesInput =
            findViewById<android.widget.EditText>(R.id.inputCalories)
        val waterInput =
            findViewById<android.widget.EditText>(R.id.inputWater)
        val saveButton =
            findViewById<android.widget.Button>(R.id.buttonSaveRecord)

        val editingRecordId = intent.getLongExtra("record_id", 0L)

        if (editingRecordId > 0) {
            val title = findViewById<android.widget.TextView>(R.id.titleAddRecord)
            title.setText(R.string.update_your_record)
            saveButton.setText(R.string.update_record)
        }

        if (editingRecordId > 0 && savedInstanceState == null) {
            val existingDate = intent.getStringExtra("record_date")

            if (existingDate != null) {
                dateButton.text = existingDate

                val parsedDate = java.time.LocalDate.parse(existingDate)
                selectedDate.set(
                    parsedDate.year,
                    parsedDate.monthValue - 1,
                    parsedDate.dayOfMonth
                )
            }

            stepsInput.setText(
                intent.getIntExtra("record_steps", 0).toString()
            )
            caloriesInput.setText(
                intent.getIntExtra("record_calories", 0).toString()
            )
            waterInput.setText(
                intent.getIntExtra("record_water", 0).toString()
            )
        }

        saveButton.setOnClickListener {
            if (dateButton.text.toString() == getString(R.string.select_date)) {
                android.widget.Toast.makeText(
                    this,
                    "Please select a date",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            fun validateNumber(field: android.widget.EditText): Int? {
                val value = field.text.toString().trim().toIntOrNull()

                field.error = when {
                    value == null -> "Enter a valid whole number"
                    value < 0 -> "Enter zero or a positive number"
                    else -> null
                }

                return if (field.error == null) value else null
            }

            val steps = validateNumber(stepsInput)
            val calories = validateNumber(caloriesInput)
            val water = validateNumber(waterInput)

            if (steps == null || calories == null || water == null) {
                return@setOnClickListener
            }

            val record = HealthRecord(
                id = editingRecordId,
                date = dateButton.text.toString(),
                steps = steps,
                calories = calories,
                water = water
            )

            viewModel.saveRecord(record)

        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.saveState.collect { state ->
                    saveButton.isEnabled =
                        state != SaveRecordState.Saving &&
                                state !is SaveRecordState.Success

                    when (state) {
                        SaveRecordState.Idle -> Unit
                        SaveRecordState.Saving -> Unit

                        is SaveRecordState.Success -> {
                            android.widget.Toast.makeText(
                                this@AddRecordActivity,
                                "Record saved",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()

                            finish()
                        }

                        is SaveRecordState.Error -> {
                            android.widget.Toast.makeText(
                                this@AddRecordActivity,
                                state.message,
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets

            }
        }
    }
