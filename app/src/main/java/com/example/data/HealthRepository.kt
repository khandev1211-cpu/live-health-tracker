package com.example.data

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.UUID
import kotlin.math.sqrt

class HealthRepository(
    private val context: Context,
    private val dao: HealthLogDao
) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val repositoryScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // --- Local DB Expose ---
    val allLogs: Flow<List<HealthLog>> = dao.getAllLogs()
    
    fun getLogsByType(type: String): Flow<List<HealthLog>> = dao.getLogsByType(type)
    fun getLogsByTypes(types: List<String>): Flow<List<HealthLog>> = dao.getLogsByTypes(types)

    suspend fun insertLog(log: HealthLog): Long = withContext(Dispatchers.IO) {
        dao.insertLog(log)
    }

    suspend fun deleteLog(log: HealthLog) = withContext(Dispatchers.IO) {
        dao.deleteLog(log)
    }

    suspend fun deleteLogById(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteLogById(id)
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        dao.clearAllLogs()
    }

    // --- On-Device Sensor Streams ---
    private val _stepCount = MutableStateFlow(0)
    val stepCount: StateFlow<Int> = _stepCount.asStateFlow()

    private val _ambientLight = MutableStateFlow(150f) // default lux
    val ambientLight: StateFlow<Float> = _ambientLight.asStateFlow()

    private var initialStepOffset = -1
    private var lastMagnitude = 0f
    private val threshold = 12.0f // accelerometer step threshold

    init {
        setupPhoneSensors()
    }

    private fun setupPhoneSensors() {
        // 1. Core Step Counter
        val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (stepSensor != null) {
            sensorManager.registerListener(object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    if (event != null && event.values.isNotEmpty()) {
                        val steps = event.values[0].toInt()
                        if (initialStepOffset == -1) {
                            initialStepOffset = steps
                        }
                        _stepCount.value = steps - initialStepOffset
                    }
                }
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }, stepSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            // Fallback: Accelerometer-based raw step detection
            val accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            if (accelSensor != null) {
                sensorManager.registerListener(object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent?) {
                        if (event != null && event.values.size >= 3) {
                            val x = event.values[0]
                            val y = event.values[1]
                            val z = event.values[2]
                            val magnitude = sqrt(x * x + y * y + z * z)
                            if (lastMagnitude > 0) {
                                val delta = Math.abs(magnitude - lastMagnitude)
                                if (delta > threshold) {
                                    _stepCount.value += 1
                                }
                            }
                            lastMagnitude = magnitude
                        }
                    }
                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
                }, accelSensor, SensorManager.SENSOR_DELAY_UI)
            }
        }

        // 2. Light Sensor for screen-time/circadian metrics
        val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        if (lightSensor != null) {
            sensorManager.registerListener(object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    if (event != null && event.values.isNotEmpty()) {
                        _ambientLight.value = event.values[0]
                    }
                }
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    // --- Interactive BLE Wearable Bluetooth Simulator ---
    private val _connectedDevice = MutableStateFlow<String?>(null)
    val connectedDevice: StateFlow<String?> = _connectedDevice.asStateFlow()

    private val _liveHeartRate = MutableStateFlow(72)
    val liveHeartRate: StateFlow<Int> = _liveHeartRate.asStateFlow()

    private val _liveSpO2 = MutableStateFlow(98)
    val liveSpO2: StateFlow<Int> = _liveSpO2.asStateFlow()

    private val _liveSleepStage = MutableStateFlow("Awake")
    val liveSleepStage: StateFlow<String> = _liveSleepStage.asStateFlow()

    private val _liveCaloriesBurned = MutableStateFlow(120.0)
    val liveCaloriesBurned: StateFlow<Double> = _liveCaloriesBurned.asStateFlow()

    private var biometricJob: Job? = null

    fun connectWearable(deviceName: String) {
        _connectedDevice.value = deviceName
        startBiometricSimulation()
    }

    fun disconnectWearable() {
        biometricJob?.cancel()
        _connectedDevice.value = null
        _liveHeartRate.value = 0
        _liveSpO2.value = 0
        _liveSleepStage.value = "Disconnected"
    }

    private fun startBiometricSimulation() {
        biometricJob?.cancel()
        biometricJob = repositoryScope.launch {
            var counter = 0
            while (isActive) {
                // Heart rate oscillates normally between 68 and 82 with noise
                val baseHr = if (_isWorkoutActive.value) 135 else 72
                val variation = (-4..4).random()
                _liveHeartRate.value = baseHr + variation

                // Save heart rate to database periodically (every 10 seconds)
                if (counter % 10 == 0) {
                    insertLog(
                        HealthLog(
                            type = "HEART_RATE",
                            timestamp = System.currentTimeMillis(),
                            value1 = _liveHeartRate.value.toDouble()
                        )
                    )
                }

                // SpO2 stays healthy 97-99%
                _liveSpO2.value = (97..99).random()

                // Sleep Stage changes based on counter cycle (Simulating daily cycles)
                _liveSleepStage.value = when ((counter / 15) % 4) {
                    0 -> "Light Sleep"
                    1 -> "Deep Sleep"
                    2 -> "REM Sleep"
                    else -> "Resting Awake"
                }

                // Simulate calories burning slightly
                _liveCaloriesBurned.value += if (_isWorkoutActive.value) 2.5 else 0.15

                // Simulate step progress slightly
                if (_isWorkoutActive.value) {
                    _stepCount.value += (8..15).random()
                }

                delay(1000)
                counter++
            }
        }
    }

    // Active Workout state
    private val _isWorkoutActive = MutableStateFlow(false)
    val isWorkoutActive: StateFlow<Boolean> = _isWorkoutActive.asStateFlow()

    fun setWorkoutActive(active: Boolean) {
        _isWorkoutActive.value = active
    }

    // --- Gemini AI Insights Agent ---
    suspend fun fetchAiHealthInsights(): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "AI Insight Unavailable: Please link your Gemini API credentials in the Google AI Studio Secrets workspace panel safely."
        }

        // Gather recent statistical telemetry logs
        val heartRates = dao.getRecentLogsByType("HEART_RATE", 10).map { it.value1.toInt() }
        val steps = dao.getRecentLogsByType("STEPS", 3).firstOrNull()?.value1?.toInt() ?: _stepCount.value
        val waterTotal = dao.getRecentLogsByType("WATER", 10).sumOf { it.value1 }
        val foodTotal = dao.getRecentLogsByType("CALORIES_IN", 10).sumOf { it.value1 }
        val sleepRecords = dao.getRecentLogsByType("SLEEP", 5)
        val avgSleep = if (sleepRecords.isNotEmpty()) sleepRecords.map { it.value1 }.average() else 7.2

        val logTelemetryPrompt = """
            Analyze the following active lifestyle metrics and provide 3 concise, bulleted health tips:
            - Average Live Heart Rate: ${if (heartRates.isNotEmpty()) heartRates.average().toInt() else 72} BPM (Vitals array: $heartRates)
            - Today's Steps Tracker: $steps steps
            - Total Water Intake: ${waterTotal.toInt()} mL
            - Nutritional Intake today: ${foodTotal.toInt()} kcal
            - Average Sleep Logs: ${String.format("%.1f", avgSleep)} hours
            - Current ambient light: ${_ambientLight.value} lux (higher screens exposure indicator)
            
            Keep the tips brief, motivating, and personalized. Explicitly add a medical disclaimer at the bottom stating you are a general wellness assistant, not a certified practitioner.
        """.trimIndent()

        val systemInstructionText = "You are a senior health analyst and cardiology counselor. Address style: informative, motivational, scientific and concise. Max 140 words."

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = logTelemetryPrompt)))),
            systemInstruction = Content(parts = listOf(Part(text = systemInstructionText))),
            generationConfig = GenerationConfig(temperature = 0.7f)
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "Pattern Analytics complete: Keep active, monitor hydrated water loops, and enjoy natural sunlight!"
        } catch (e: Exception) {
            Log.e("HealthRepo", "Gemini API error", e)
            "AI Insights Cache: Hydrate efficiently, maintain a balanced exercise habit of 8,000 steps daily, and sleep consistent hours.\n\nDisclaimer: Not a medical advice alternative."
        }
    }
}
