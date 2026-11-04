package com.dasuni.healthmate

data class HealthRecord(
    val id: Long = 0,
    val date: String,
    val steps: Int,
    val calories: Int,
    val water: Int
)