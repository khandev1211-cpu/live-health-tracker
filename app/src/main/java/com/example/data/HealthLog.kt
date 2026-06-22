package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "health_logs")
data class HealthLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // e.g. "HEART_RATE", "STEPS", "WATER", "CALORIES_IN", "CALORIES_OUT", "SLEEP", "BLOOD_PRESSURE", "BLOOD_SUGAR", "MOOD", "MEDICATION", "SPO2", "WEIGHT"
    val timestamp: Long,
    val value1: Double, // main numeric parameter
    val value2: Double = 0.0, // secondary numeric parameter
    val notes: String = "" // text details (meal names, food item, mood description, medication Name)
)
