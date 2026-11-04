package com.dasuni.healthmate

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        findViewById<android.widget.Button>(R.id.buttonTodaySummary)
            .setOnClickListener {
                val intent = android.content.Intent(this, SummaryActivity::class.java)
                startActivity(intent)
            }

        val historyButton =
            findViewById<android.widget.Button>(R.id.buttonViewHistory)

        historyButton.setOnClickListener {
            val intent = android.content.Intent(
                this,
                RecordHistoryActivity::class.java
            )
            startActivity(intent)
        }

        val addRecordButton =
            findViewById<android.widget.Button>(R.id.buttonAddRecord)

        addRecordButton.setOnClickListener {
            val intent = android.content.Intent(
                this,
                AddRecordActivity::class.java
            )
            startActivity(intent)
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
}