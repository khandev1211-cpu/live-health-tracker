# PULSE 3D - Live Health Tracker

<div align="center">
<img width="1200" height="475" alt="Health Tracker Banner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />
</div>

**PULSE 3D** is a comprehensive Android health & wellness tracking application built with **Jetpack Compose** and **Material 3**. It features real-time biometric monitoring, on-device sensor integration, AI-powered health insights via **Google Gemini**, and an immersive cyberpunk-themed dark UI.

> ⚠️ **Medical Disclaimer:** This application is a general wellness tracker and is **NOT** certified as a medical device. It must not be substituted for clinical diagnostic devices or professional healthcare guidance.

---

## ✨ Features

### 📊 Dashboard
- **Live Biometric Pulse Engine** — Central animated 3D heart rate orb that pulses in sync with your BPM
- **Real-time PPG Cardiac Wave** — Live electrocardiogram-style graph of heart rate history
- **Smart Alerts** — Automated health warnings for elevated/low heart rate, low SpO₂, dehydration, inactivity, and medication compliance
- **Goal Progress Rings** — Animated 3D circular progress indicators for steps and hydration
- **Gemini AI Insights** — Google Gemini-powered health analysis based on your daily logged metrics

### 🏃 Activity Tracking
- **Step Counter** — Uses the device's built-in step counter sensor (with accelerometer fallback)
- **Ambient Light Sensor** — Circadian rhythm tracking via light sensor
- **Workout Mode** — Simulated GPS route tracking with live heart rate, timer, and calorie expenditure
- **Distance & Calorie Estimation**

### 💤 Sleep Monitoring
- **Live Sleep Stage Tracking** — Real-time wearable sleep stage streaming (Light, Deep, REM, Awake)
- **Sleep Stage Breakdown** — Visual bar chart with optimal sleep stage percentages
- **Manual Sleep Logging** — Log hours slept and quality percentage
- **Historical Sleep Records** — View past sleep entries from local database

### 🥗 Nutrition Tracking
- **Hydration Cylinder** — Animated 3D water container with wave effects showing daily intake
- **Quick Water Logging** — +250mL and +500mL quick-add buttons
- **Meal Diary** — Log food items with calorie counts
- **Calorie Progress Bar** — Visual indicator of daily caloric intake vs. goal

### ❤️ Vitals & Biometrics
- **Camera PPG Heart Rate Scanner** — Experimental photo-plethysmography using the rear camera and flash to measure BPM via fingertip
- **Fingerprint Tap Assist** — Manual tap-based pulse rate estimation
- **Blood Pressure Logging** — Record systolic/diastolic readings
- **Blood Glucose Logging** — Record fasting/post-meal glucose levels
- **Mood & Stress Tracking** — 1-10 scale with symptom notes

### ⚙️ Settings & Configuration
- **BLE Wearable Simulator** — Connect simulated smart bands (FitPulse v2, Cardio3D Watch) for live biometric streaming
- **Custom Goals** — Configure daily targets for steps, water intake, sleep hours, and calories
- **Permission Status** — View granted permissions at a glance
- **Database Maintenance** — Clear all cached health logs from local Room database

---

## 🧰 Tech Stack

| Component | Technology |
|-----------|-----------|
| **UI Framework** | Jetpack Compose + Material 3 |
| **Navigation** | Custom tab-based navigation (no Navigation Compose) |
| **Architecture** | MVVM with AndroidViewModel |
| **Local Database** | Room (SQLite) with Flow-based reactive queries |
| **AI Integration** | Google Gemini API (via Retrofit + Moshi) |
| **Camera** | Camera2 API for PPG heart rate scanning |
| **Sensors** | Step Counter, Accelerometer, Light Sensor |
| **HTTP Client** | OkHttp + Retrofit |
| **JSON Parsing** | Moshi (with Kotlin codegen) |
| **Coroutines** | Kotlin Coroutines + StateFlow |
| **Image Loading** | Coil Compose |
| **Permissions** | Accompanist Permissions API |
| **Testing** | JUnit, Robolectric, Roborazzi (Compose screenshot tests) |
| **Build System** | Gradle with Kotlin DSL + Version Catalog |

---

## 🏗️ Project Architecture

```
com.example/
├── MainActivity.kt              # Entry point with bottom navigation + top header
├── data/
│   ├── AppDatabase.kt           # Room database singleton
│   ├── GeminiService.kt         # Gemini API client (Retrofit + Moshi)
│   ├── HealthLog.kt             # Room entity for all health records
│   ├── HealthLogDao.kt          # Room DAO with Flow queries
│   └── HealthRepository.kt      # Repository: sensors, BLE simulator, Gemini AI
└── ui/
    ├── HealthViewModel.kt       # Shared ViewModel for all screens
    ├── DashboardScreen.kt       # Main dashboard with pulse orb, graph, alerts, AI
    ├── ActivityScreen.kt        # Step counter, workout mode, GPS logging
    ├── SleepScreen.kt           # Sleep stage tracking, manual logging
    ├── NutritionScreen.kt       # Water tracker, meal diary
    ├── VitalsScreen.kt          # PPG scanner, tap assist, BP, glucose, mood
    ├── SettingsScreen.kt        # Wearable pairing, goals config, permissions, data wipe
    ├── HealthUiComponents.kt    # Reusable: glowing orb, goal rings, metric cards, ECG graph
    ├── PulseSensorComposables.kt # Dialog-based PPG and fingerprint sensor UIs
    ├── CameraPpgAnalyzer.kt     # Camera2 backend for PPG signal processing & peak detection
    └── theme/
        ├── Color.kt             # Dark-tech neon color palette
        ├── Theme.kt             # Material 3 dark theme configuration
        └── Type.kt              # Typography definitions
```

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio** (Latest stable version recommended)
- **Android SDK** 36 (compile SDK)
- **Minimum SDK**: 24 (Android 7.0 Nougat)
- **Target SDK**: 36

### Setup Instructions

1. **Clone the repository**
   ```bash
   git clone https://github.com/khandev1211-cpu/live-health-tracker.git
   ```

2. **Open in Android Studio**
   - Select **File → Open** and choose the project directory
   - Allow Android Studio to sync Gradle and resolve dependencies

3. **Configure Gemini API Key**
   - Create a `.env` file in the project root directory (copy from `.env.example`)
   - Add your Gemini API key:
     ```
     GEMINI_API_KEY=your_gemini_api_key_here
     ```
   - Get your API key from [Google AI Studio](https://makersuite.google.com/app/apikey)

4. **Build & Run**
   - Select a device or emulator
   - Click **Run** (or use `./gradlew assembleDebug`)
   - The app requires no additional hardware — sensors gracefully fall back to simulation mode on emulators

---

## 🔐 Permissions

The app requests the following permissions:

| Permission | Purpose |
|-----------|---------|
| `INTERNET` | Gemini API calls |
| `CAMERA` | PPG heart rate scanning |
| `BODY_SENSORS` | Wearable integration |
| `ACTIVITY_RECOGNITION` | Step counter sensor |
| `ACCESS_FINE_LOCATION` | GPS route tracking during workouts |
| `ACCESS_COARSE_LOCATION` | Approximate location for workout logging |
| `POST_NOTIFICATIONS` | Health alerts and reminders |

---

## 📱 Screens Overview

| Screen | Description |
|--------|-------------|
| **Dashboard** | Central hub: pulse orb (animates at your BPM), ECG graph, goal rings, smart alerts panel, Gemini AI insights |
| **Activity** | Step count, distance, calories burned, ambient lux, workout mode with simulated GPS tracking |
| **Sleep** | Live wearable sleep stage, sleep stage breakdown chart, manual sleep logger, historical records |
| **Nutrition** | 3D animated water cylinder with wave physics, quick-add hydration buttons, meal diary with calorie tracking |
| **Vitals** | Camera PPG scanner (15-sec reading via fingertip over flash), tap-assist pulse estimator, BP/glucose/mood logging |
| **Settings** | Wearable device pairing simulator, custom goal calibration, permission indicators, database wipe |

---

## 🔧 Technical Highlights

### PPG Heart Rate Detection (Camera2)
The app implements a real-time **Photo-Plethysmography (PPG)** algorithm:
- Captures YUV_420_888 frames from the rear camera at 25 FPS
- Extracts red chrominance (V/Cr channel) to detect blood volume pulses
- Applies a 5-point moving average filter for noise reduction
- Uses peak detection with sliding window to identify systolic peaks
- Converts average peak-to-peak intervals to BPM
- Falls back to a high-fidelity simulated waveform on emulators

### On-Device Sensor Fusion
- **Step Counter**: Uses `TYPE_STEP_COUNTER` sensor with offset tracking
- **Accelerometer Fallback**: If step counter unavailable, detects steps via magnitude threshold crossing
- **Light Sensor**: Tracks ambient lux for circadian rhythm context

### BLE Wearable Simulator
- Simulates connected wearables with realistic biometric streaming
- Heart rate oscillates with natural variation (68-82 BPM resting, ~135 during workout)
- SpO₂ stays in healthy 97-99% range
- Sleep stages cycle through Light → Deep → REM → Awake

### Gemini AI Integration
- Sends aggregated daily metrics (heart rate, steps, water, calories, sleep) to Gemini
- Receives personalized, bulleted health tips
- Includes medical disclaimer in every response
- Configurable via `.env` file for API key security

---

## 📦 Dependencies

Key dependencies managed via Gradle Version Catalog (`gradle/libs.versions.toml`):

- **AndroidX Core** 1.18.0
- **Jetpack Compose** (BOM 2024.09.00)
- **Material 3** with extended icons
- **Room** 2.7.0 (with KSP compiler)
- **Retrofit** 2.12.0 + Moshi converter
- **OkHttp** 4.10.0 (with logging interceptor)
- **Firebase AI** (Gemini API)
- **Google Play Services Location** 21.3.0
- **Coil Compose** 2.7.0
- **Roborazzi** 1.59.0 (visual testing)

---

## 🧪 Testing

The project includes setup for:
- **Unit Tests**: JUnit 4 + Robolectric
- **UI Tests**: Compose UI Test with JUnit4
- **Visual Regression**: Roborazzi for Compose screenshot comparison testing

Run tests with:
```bash
./gradlew test
./gradlew connectedCheck
```

---

## 📄 License

This project is built with Google AI Studio. View your app in AI Studio: https://ai.studio/apps/aa7db216-b232-485c-8201-4b7169046649

---

## 🙏 Acknowledgments

- Built with [Google AI Studio](https://ai.studio)
- Powered by [Gemini API](https://ai.google.dev/)
- Jetpack Compose & Material 3 by Android