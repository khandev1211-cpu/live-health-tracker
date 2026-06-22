package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.HealthLog
import com.example.data.HealthRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HealthViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = HealthRepository(application, database.healthLogDao())

    // --- State Expose ---
    val allLogs: StateFlow<List<HealthLog>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stepCount: StateFlow<Int> = repository.stepCount
    val ambientLight: StateFlow<Float> = repository.ambientLight
    val connectedDevice: StateFlow<String?> = repository.connectedDevice
    val liveHeartRate: StateFlow<Int> = repository.liveHeartRate
    val liveSpO2: StateFlow<Int> = repository.liveSpO2
    val liveSleepStage: StateFlow<String> = repository.liveSleepStage
    val liveCaloriesBurned: StateFlow<Double> = repository.liveCaloriesBurned
    val isWorkoutActive: StateFlow<Boolean> = repository.isWorkoutActive

    // --- Navigation ---
    private val _currentScreen = MutableStateFlow("dashboard") // dashboard, activity, sleep, nutrition, vitals, settings
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    // --- Daily Custom Goals / Targets ---
    val stepsGoal = MutableStateFlow(10000)
    val waterGoal = MutableStateFlow(2500)
    val sleepGoal = MutableStateFlow(8.0)
    val caloriesGoal = MutableStateFlow(2200)

    // --- Smart Alerts State ---
    private val _smartAlerts = MutableStateFlow<List<SmartAlert>>(emptyList())
    val smartAlerts: StateFlow<List<SmartAlert>> = _smartAlerts.asStateFlow()

    // --- AI Insights State ---
    private val _aiInsights = MutableStateFlow("Load AI Insights to view automated diagnostics from your daily biometric loops...")
    val aiInsights: StateFlow<String> = _aiInsights.asStateFlow()

    private val _isLoadingInsights = MutableStateFlow(false)
    val isLoadingInsights: StateFlow<Boolean> = _isLoadingInsights.asStateFlow()

    init {
        // Automatically check/generate standard alerts relative to live telemetry
        viewModelScope.launch {
            combine(
                stepCount,
                liveHeartRate,
                liveSpO2,
                allLogs
            ) { steps, hr, spo2, logs ->
                generateAlertsList(steps, hr, spo2, logs)
            }.collect { alerts ->
                _smartAlerts.value = alerts
            }
        }
    }

    private fun generateAlertsList(steps: Int, hr: Int, spo2: Int, logs: List<HealthLog>): List<SmartAlert> {
        val alerts = mutableListOf<SmartAlert>()

        // 1. Cardiovascular warning
        if (hr > 120 && !isWorkoutActive.value) {
            alerts.add(
                SmartAlert(
                    title = "Elevated Heart Rate Warning",
                    message = "PPG sensor detects $hr BPM while resting. Take deep breaths.",
                    severity = "high"
                )
            )
        } else if (hr in 1..49) {
            alerts.add(
                SmartAlert(
                    title = "Low Heart Rate Notice",
                    message = "PPG sensor reports low heart rate: $hr BPM. Consult with clinical guidance.",
                    severity = "medium"
                )
            )
        }

        // 2. Oxygen SpO2 hazard
        if (spo2 in 1..92) {
            alerts.add(
                SmartAlert(
                    title = "Abnormal SpO2 Alert",
                    message = "Biometric arterial oxygen concentration is low ($spo2%). Reposition smartwear.",
                    severity = "high"
                )
            )
        }

        // 3. Dehydration tracker
        val waterIntakeTotal = logs.filter { it.type == "WATER" && isToday(it.timestamp) }.sumOf { it.value1 }
        if (waterIntakeTotal < 1000) {
            alerts.add(
                SmartAlert(
                    title = "Hydration Reminder",
                    message = "You only logged ${waterIntakeTotal.toInt()} mL of water today. Please drink a glass soon.",
                    severity = "medium"
                )
            )
        }

        // 4. Inactivity tracking
        if (steps < 2000 && System.currentTimeMillis() % 86400000 > 50400000) { // afternoon inactivity
            alerts.add(
                SmartAlert(
                    title = "Sedentary Activity Level",
                    message = "Step progression is under 2,000 steps ($steps steps). Stand up and stretch!",
                    severity = "medium"
                )
            )
        }

        // 5. Medication compliance checker
        val loggedMedsToday = logs.filter { it.type == "MEDICATION" && isToday(it.timestamp) }
        if (loggedMedsToday.isEmpty()) {
            alerts.add(
                SmartAlert(
                    title = "Medication Log Missing",
                    message = "No medication compliance logged today. Tap Logs to medication tracker.",
                    severity = "low"
                )
            )
        }

        return alerts
    }

    private fun isToday(timeMs: Long): Boolean {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        return sdf.format(Date(timeMs)) == sdf.format(Date())
    }

    // --- Interactive Manual Insertion Operations ---
    fun logWater(ml: Double) {
        viewModelScope.launch {
            repository.insertLog(
                HealthLog(
                    type = "WATER",
                    timestamp = System.currentTimeMillis(),
                    value1 = ml
                )
            )
        }
    }

    fun logMeal(calories: Double, name: String) {
        viewModelScope.launch {
            repository.insertLog(
                HealthLog(
                    type = "CALORIES_IN",
                    timestamp = System.currentTimeMillis(),
                    value1 = calories,
                    notes = name
                )
            )
        }
    }

    fun logBloodPressure(systolic: Double, diastolic: Double) {
        viewModelScope.launch {
            repository.insertLog(
                HealthLog(
                    type = "BLOOD_PRESSURE",
                    timestamp = System.currentTimeMillis(),
                    value1 = systolic,
                    value2 = diastolic
                )
            )
        }
    }

    fun logBloodSugar(glucose: Double, state: String) {
        viewModelScope.launch {
            repository.insertLog(
                HealthLog(
                    type = "BLOOD_SUGAR",
                    timestamp = System.currentTimeMillis(),
                    value1 = glucose,
                    notes = state
                )
            )
        }
    }

    fun logSleep(duration: Double, quality: Int) {
        viewModelScope.launch {
            repository.insertLog(
                HealthLog(
                    type = "SLEEP",
                    timestamp = System.currentTimeMillis(),
                    value1 = duration,
                    value2 = quality.toDouble()
                )
            )
        }
    }

    fun logMood(rating: Int, note: String) {
        viewModelScope.launch {
            repository.insertLog(
                HealthLog(
                    type = "MOOD",
                    timestamp = System.currentTimeMillis(),
                    value1 = rating.toDouble(),
                    notes = note
                )
            )
        }
    }

    fun logMedication(name: String, status: Boolean) {
        viewModelScope.launch {
            repository.insertLog(
                HealthLog(
                    type = "MEDICATION",
                    timestamp = System.currentTimeMillis(),
                    value1 = if (status) 1.0 else 0.0,
                    notes = name
                )
            )
        }
    }

    fun logSpO2(pct: Double) {
        viewModelScope.launch {
            repository.insertLog(
                HealthLog(
                    type = "SPO2",
                    timestamp = System.currentTimeMillis(),
                    value1 = pct
                )
            )
        }
    }

    fun logHeartRate(bpm: Double, source: String) {
        viewModelScope.launch {
            repository.insertLog(
                HealthLog(
                    type = "HEART_RATE",
                    timestamp = System.currentTimeMillis(),
                    value1 = bpm,
                    notes = source
                )
            )
        }
    }

    fun logWeight(kg: Double) {
        val heightInMeters = 1.75 // standard baseline height reference
        val bmi = kg / (heightInMeters * heightInMeters)
        viewModelScope.launch {
            repository.insertLog(
                HealthLog(
                    type = "WEIGHT",
                    timestamp = System.currentTimeMillis(),
                    value1 = kg,
                    value2 = bmi
                )
            )
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    // --- Hardware and Device Sync Control ---
    fun connectWearable(name: String) = repository.connectWearable(name)
    fun disconnectWearable() = repository.disconnectWearable()
    fun toggleWorkout() = repository.setWorkoutActive(!isWorkoutActive.value)

    // --- AI Insight Service Trigger ---
    fun fetchAiInsights() {
        _isLoadingInsights.value = true
        _aiInsights.value = "Requesting biometric patterns diagnostics from Gemini..."
        viewModelScope.launch {
            try {
                val response = repository.fetchAiHealthInsights()
                _aiInsights.value = response
            } catch (e: Exception) {
                _aiInsights.value = "AI analytics failed: Hydrate securely, log entries frequently, and protect cardiovascular loops!"
            } finally {
                _isLoadingInsights.value = false
            }
        }
    }
}

data class SmartAlert(
    val title: String,
    val message: String,
    val severity: String // high, medium, low
)
