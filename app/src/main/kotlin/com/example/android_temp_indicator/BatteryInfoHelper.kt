package com.example.android_temp_indicator

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager

enum class TemperatureStatus(val label: String, val description: String) {
    COOL("COOL", "< 35°C — Optimal & safe"),
    WARM("WARM", "35°C – 39.9°C — Normal usage"),
    HOT("HOT", "40°C – 44.9°C — Heavy load / Fast charging"),
    VERY_HOT("VERY HOT", "≥ 45°C — High heat, cool down recommended")
}

data class BatteryData(
    val temperature: Double,
    val percentage: Int,
    val isCharging: Boolean,
    val voltage: Double?,
    val health: String?,
    val chargingType: String?
) {
    val status: TemperatureStatus
        get() = when {
            temperature < 35.0 -> TemperatureStatus.COOL
            temperature < 40.0 -> TemperatureStatus.WARM
            temperature < 45.0 -> TemperatureStatus.HOT
            else -> TemperatureStatus.VERY_HOT
        }
}

object BatteryInfoHelper {

    fun getBatteryData(context: Context): BatteryData {
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus: Intent? = context.registerReceiver(null, intentFilter)
        return parseBatteryIntent(batteryStatus)
    }

    fun parseBatteryIntent(intent: Intent?): BatteryData {
        if (intent == null) {
            return BatteryData(
                temperature = 0.0,
                percentage = 0,
                isCharging = false,
                voltage = null,
                health = "Unknown",
                chargingType = "Unknown"
            )
        }

        // Temperature: Android commonly reports in tenths of a degree Celsius (e.g. 385 -> 38.5)
        // Some devices/kernels may report in millidegrees (e.g. 38500 -> 38.5)
        val rawTemp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
        val temperature = when {
            rawTemp > 1000 -> rawTemp / 1000.0
            rawTemp > 0 -> rawTemp / 10.0
            else -> 0.0
        }

        // Battery percentage
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val percentage = if (level >= 0 && scale > 0) {
            (level * 100 / scale)
        } else {
            0
        }

        // Charging status
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        // Voltage: usually in mV -> convert to V (e.g. 4180 -> 4.18V)
        val rawVoltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
        val voltage = if (rawVoltage > 0) {
            if (rawVoltage > 1000) rawVoltage / 1000.0 else rawVoltage.toDouble()
        } else {
            null
        }

        // Battery Health
        val healthCode = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)
        val health = when (healthCode) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Unspecified Failure"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            else -> "Unknown"
        }

        // Charging Type (Plugged)
        val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val chargingType = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> "AC Charger"
            BatteryManager.BATTERY_PLUGGED_USB -> "USB"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
            else -> if (isCharging) "Charging" else "Discharging"
        }

        return BatteryData(
            temperature = temperature,
            percentage = percentage,
            isCharging = isCharging,
            voltage = voltage,
            health = health,
            chargingType = chargingType
        )
    }
}
