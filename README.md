# Battery Temperature Monitor (Android)

[![Latest Release](https://img.shields.io/github/v/release/AhmadHasta/android_temp_indicator?color=blue&logo=github)](https://github.com/AhmadHasta/android_temp_indicator/releases/latest)
[![Download APK](https://img.shields.io/badge/Download-APK%20(Release)-success?logo=android&logoColor=white)](https://github.com/AhmadHasta/android_temp_indicator/releases/latest)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A lightweight, battery-efficient Android battery temperature monitor using **Flutter** for the dashboard UI and **Kotlin Foreground Service** for continuous background monitoring directly in the Android status bar.

---

## 📥 Download & Installation

You can download and install the application directly onto your Android device without compiling:

👉 **[Download Latest APK from GitHub Releases](https://github.com/AhmadHasta/android_temp_indicator/releases/latest)**

1. Open the [Releases](https://github.com/AhmadHasta/android_temp_indicator/releases/latest) page.
2. Scroll to the **Assets** section.
3. Tap on `battery-temperature-monitor-v1.0.0.apk` to download.
4. Open the downloaded file to install on your Android device (allow *Install unknown apps* if prompted).

---

## ✨ Key Features

- **Live Status Bar Indicator**: Displays the current battery temperature (e.g. `38°`) as a dynamic monochrome small notification icon in the system status bar without needing custom overlay windows (`SYSTEM_ALERT_WINDOW`).
- **Independent Native Background Service**: The Flutter UI can be completely closed; the Kotlin Foreground Service continues monitoring independently without keeping the Flutter engine alive in memory.
- **Battery & CPU Efficient**: Event-driven battery monitoring via `Intent.ACTION_BATTERY_CHANGED`. Notification and status bar icons only re-render when temperature or charging state changes.
- **Modern Dashboard**:
  - Real-time battery temperature with color-coded classification (`COOL`, `WARM`, `HOT`, `VERY HOT`).
  - Battery percentage, charging state, voltage, and health status.
  - Quick toggle to start/stop monitoring.
  - HyperOS / Xiaomi battery optimization tips.
- **Modern Android Compatibility**: Android 13+ runtime notification permissions (`POST_NOTIFICATIONS`) and Android 14+ foreground service types (`FOREGROUND_SERVICE_SPECIAL_USE`).

---

## 📱 Screenshots & Architecture

```text
┌─────────────────────────────────────────────┐
│ 12:30       📶 38°       🔋 72%             │  <- Status Bar Small Icon
└─────────────────────────────────────────────┘
```

```text
┌───────────────────────────────┐
│           Flutter UI          │
│                               │
│ Dashboard / Controls          │
└───────────────┬───────────────┘
                │ MethodChannel ("battery_monitor")
                ▼
┌───────────────────────────────┐
│        Android / Kotlin       │
│                               │
│ BatteryInfoHelper             │
│ TemperatureIconGenerator      │
│ BatteryMonitorService (FGS)   │
└───────────────┬───────────────┘
                │
                ▼
      Android BatteryManager
```

---

## 🚀 Getting Started (Development)

### Prerequisites
- Flutter SDK (>= 3.12.0)
- Android SDK (API 21+)
- A physical Android device or emulator (tested on Xiaomi Poco F5 / HyperOS)

### Running the App
```bash
# Get dependencies
flutter pub get

# Run on connected Android device
flutter run
```

### Building Release APK
```bash
flutter build apk --release
```

---

## ⚙️ Xiaomi / Poco (HyperOS) Setup Tips
To ensure uninterrupted background monitoring on HyperOS / MIUI:
1. **Battery Saver**: App Info > Battery saver > Choose **"No restrictions"**.
2. **Autostart**: App Info > Enable **"Autostart"**.
3. **Lock in Recent Apps**: Recent Apps screen > Long press the app > Tap the **Lock** icon.

---

## 📄 License
MIT License.
