package com.example.android_temp_indicator

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

/**
 * Receiver that re-pins the ongoing notification if a user attempts to dismiss/swipe it away on Android 14+.
 */
class BatteryMonitorDismissReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (BatteryMonitorService.isMonitoringActive(context)) {
            val serviceIntent = Intent(context, BatteryMonitorService::class.java).apply {
                action = BatteryMonitorService.ACTION_START
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            } catch (e: Exception) {
                // Ignore if background start is restricted
            }
        }
    }
}
