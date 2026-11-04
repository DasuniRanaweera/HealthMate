package com.dasuni.healthmate

import android.content.ContentValues
import android.content.Context
import java.time.LocalDate

class HealthRecordRepository(context: Context) {

    private val appContext = context.applicationContext

    fun addRecord(record: HealthRecord): Long {
        LocalDate.parse(record.date)
        require(record.steps >= 0)
        require(record.calories >= 0)
        require(record.water >= 0)

        val values = ContentValues().apply {
            put("date", record.date)
            put("steps", record.steps)
            put("calories", record.calories)
            put("water", record.water)
        }

        return HealthDatabaseHelper(appContext).use { helper ->
            helper.writableDatabase.insertOrThrow(
                "health_records",
                null,
                values
            )
        }
    }

    fun getRecords(): List<HealthRecord> {
        return HealthDatabaseHelper(appContext).use { helper ->
            helper.readableDatabase.query(
                "health_records",
                arrayOf("id", "date", "steps", "calories", "water"),
                null,
                null,
                null,
                null,
                "date DESC, id DESC"
            ).use { cursor ->
                val records = mutableListOf<HealthRecord>()

                val idIndex = cursor.getColumnIndexOrThrow("id")
                val dateIndex = cursor.getColumnIndexOrThrow("date")
                val stepsIndex = cursor.getColumnIndexOrThrow("steps")
                val caloriesIndex =
                    cursor.getColumnIndexOrThrow("calories")
                val waterIndex = cursor.getColumnIndexOrThrow("water")

                while (cursor.moveToNext()) {
                    records.add(
                        HealthRecord(
                            id = cursor.getLong(idIndex),
                            date = cursor.getString(dateIndex),
                            steps = cursor.getInt(stepsIndex),
                            calories = cursor.getInt(caloriesIndex),
                            water = cursor.getInt(waterIndex)
                        )
                    )
                }

                records
            }
        }
    }

    fun deleteRecord(recordId: Long): Int {
        require(recordId > 0)

        return HealthDatabaseHelper(appContext).use { helper ->
            helper.writableDatabase.delete(
                "health_records",
                "id = ?",
                arrayOf(recordId.toString())
            )
        }
    }

    fun updateRecord(record: HealthRecord): Int {
        require(record.id > 0)
        LocalDate.parse(record.date)
        require(record.steps >= 0)
        require(record.calories >= 0)
        require(record.water >= 0)

        val values = ContentValues().apply {
            put("date", record.date)
            put("steps", record.steps)
            put("calories", record.calories)
            put("water", record.water)
        }

        return HealthDatabaseHelper(appContext).use { helper ->
            helper.writableDatabase.update(
                "health_records",
                values,
                "id = ?",
                arrayOf(record.id.toString())
            )
        }
    }
}