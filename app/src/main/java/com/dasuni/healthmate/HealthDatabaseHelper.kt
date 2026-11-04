package com.dasuni.healthmate

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class HealthDatabaseHelper(context: Context) :
    SQLiteOpenHelper(
        context.applicationContext,
        "healthmate.db",
        null,
        1
    ) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE health_records (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                date TEXT NOT NULL,
                steps INTEGER NOT NULL CHECK (steps >= 0),
                calories INTEGER NOT NULL CHECK (calories >= 0),
                water INTEGER NOT NULL CHECK (water >= 0)
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        throw IllegalStateException(
            "Database migration required from $oldVersion to $newVersion"
        )
    }
}
