package com.example.android_temp_indicator

import android.content.Context
import android.content.SharedPreferences

data class DisplayPreferences(
    val showBatteryLevel: Boolean = true,
    val showChargingStatus: Boolean = true,
    val showVoltage: Boolean = true
)

object BatteryPreferences {
    private const val PREFS_NAME = "battery_monitor_display_prefs"
    private const val KEY_SHOW_BATTERY_LEVEL = "key_show_battery_level"
    private const val KEY_SHOW_CHARGING_STATUS = "key_show_charging_status"
    private const val KEY_SHOW_VOLTAGE = "key_show_voltage"

    fun getPreferences(context: Context): DisplayPreferences {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return DisplayPreferences(
            showBatteryLevel = sp.getBoolean(KEY_SHOW_BATTERY_LEVEL, true),
            showChargingStatus = sp.getBoolean(KEY_SHOW_CHARGING_STATUS, true),
            showVoltage = sp.getBoolean(KEY_SHOW_VOLTAGE, true)
        )
    }

    fun savePreferences(context: Context, prefs: DisplayPreferences) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_SHOW_BATTERY_LEVEL, prefs.showBatteryLevel)
            .putBoolean(KEY_SHOW_CHARGING_STATUS, prefs.showChargingStatus)
            .putBoolean(KEY_SHOW_VOLTAGE, prefs.showVoltage)
            .apply()
    }
}
