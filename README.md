# Battery Temperature Monitor (Android Native)

[![Latest Release](https://img.shields.io/github/v/release/AhmadHasta/android_temp_indicator?color=blue&logo=github)](https://github.com/AhmadHasta/android_temp_indicator/releases/latest)
[![Download APK](https://img.shields.io/badge/Download-APK%20(Release)-success?logo=android&logoColor=white)](https://github.com/AhmadHasta/android_temp_indicator/releases/latest)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A lightweight, ultra-efficient Android battery temperature monitor built **100% natively in Kotlin** using **Jetpack Compose (Material 3)** for the dashboard UI and a **Kotlin Foreground Service** for continuous background monitoring directly in the Android status bar.

---

## 📥 Download & Installation

You can download and install the application directly onto your Android device without compiling:

👉 **[Download Latest APK from GitHub Releases](https://github.com/AhmadHasta/android_temp_indicator/releases/latest)**

1. Open the [Releases](https://github.com/AhmadHasta/android_temp_indicator/releases/latest) page.
2. Scroll to the **Assets** section.
3. Tap on `battery-temperature-monitor-v1.0.0.apk` (or latest version) to download (< 1 MB).
4. Open the downloaded file to install on your Android device (allow *Install unknown apps* if prompted).

---

## ✨ Key Features

- **Ultra Lightweight (< 1 MB APK)**: Migrated from hybrid framework to pure Kotlin Android Native. Zero engine bloat, instantaneous startup time, and minimal memory footprint.
- **Live Status Bar Indicator**: Displays the current battery temperature (e.g. `38°`) as a dynamic monochrome small notification icon in the system status bar without needing custom overlay windows (`SYSTEM_ALERT_WINDOW`).
- **Native Background Service**: The app UI can be completely closed or swiped away from recent apps; the Kotlin Foreground Service continues monitoring independently.
- **Real-Time & Battery Efficient**: Event-driven battery monitoring via `Intent.ACTION_BATTERY_CHANGED`. Notification and status bar icons only re-render when temperature or charging state changes.
- **Modern Jetpack Compose UI**:
  - Real-time battery temperature with color-coded classification (`COOL`, `WARM`, `HOT`, `VERY HOT`).
  - Battery percentage, charging state & type, voltage, and health status.
  - Quick toggle to start/stop monitoring.
  - Status bar visual preview.
  - HyperOS / Xiaomi battery optimization tips.
- **Modern Android Compatibility**: Android 13+ runtime notification permissions (`POST_NOTIFICATIONS`) and Android 14+ foreground service types (`FOREGROUND_SERVICE_SPECIAL_USE`).

---

## 📱 Status Bar & Architecture

```text
┌─────────────────────────────────────────────┐
│ 12:30 38°                   📶 🔋 72%       │  <- Live Status Bar Temperature Icon
└─────────────────────────────────────────────┘
```

> **Note on Status Bar Placement**: The temperature indicator appears directly beside the clock in the Android status bar notification icon area using a dynamic monochrome `smallIcon`. This leverages Android's native `ForegroundService` and `NotificationManager` for lightweight, battery-efficient operation without requiring intrusive floating overlay (`SYSTEM_ALERT_WINDOW`) permissions.

```text
┌───────────────────────────────────────────────┐
│     Jetpack Compose Dashboard (UI Layer)      │
│  - Real-time State & Theme (Material 3)       │
│  - Live BroadcastReceiver in Foreground       │
└───────────────────────┬───────────────────────┘
                        │
                        ▼
┌───────────────────────────────────────────────┐
│          Kotlin Native Architecture           │
│  - BatteryInfoHelper                          │
│  - TemperatureIconGenerator (Bitmap Canvas)   │
│  - BatteryMonitorService (Foreground Service) │
└───────────────────────┬───────────────────────┘
                        │
                        ▼
             Android BatteryManager
```

---

## 🚀 Getting Started (Development)

### Prerequisites
- Android Studio Ladybug / Meerkat or newer (or JDK 17/21 + Android SDK)
- Android SDK (API 26+)
- A physical Android device or emulator (tested on Xiaomi Poco F5 / HyperOS)

### Building the Project

Open the root folder directly in **Android Studio**, or build via command line:

```bash
# Compile and build Debug APK
./gradlew assembleDebug

# Compile and build optimized Release APK (< 1 MB)
./gradlew assembleRelease
```

The release APK will be generated at:
`app/build/outputs/apk/release/app-release.apk`

---

## ⚙️ Xiaomi / Poco (HyperOS) Setup Tips

To ensure uninterrupted background monitoring on HyperOS / MIUI:
1. **Battery Saver**: App Info > Battery saver > Choose **"No restrictions"**.
2. **Autostart**: App Info > Enable **"Autostart"**.
3. **Lock in Recent Apps**: Recent Apps screen > Long press the app > Tap the **Lock** icon.

---

## 📄 License
MIT License.
