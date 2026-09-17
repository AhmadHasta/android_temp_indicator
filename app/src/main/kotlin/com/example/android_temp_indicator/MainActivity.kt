package com.example.android_temp_indicator

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.example.android_temp_indicator.ui.DashboardScreen
import com.example.android_temp_indicator.ui.theme.BatteryMonitorTheme

class MainActivity : ComponentActivity() {

    private var batteryData by mutableStateOf(
        BatteryData(
            temperature = 0.0,
            percentage = 0,
            isCharging = false,
            voltage = null,
            health = "Unknown",
            chargingType = "Unknown"
        )
    )

    private var isMonitoring by mutableStateOf(false)
    private var hasNotificationPermission by mutableStateOf(true)
    private var displayPreferences by mutableStateOf(DisplayPreferences())

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted && !isMonitoring) {
            startMonitoringService()
        }
    }

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                batteryData = BatteryInfoHelper.parseBatteryIntent(intent)
            }
        }
    }
    private var isReceiverRegistered = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        displayPreferences = BatteryPreferences.getPreferences(this)
        checkNotificationPermission()
        refreshBatteryState()

        setContent {
            BatteryMonitorTheme(darkTheme = true) {
                DashboardScreen(
                    batteryData = batteryData,
                    isMonitoring = isMonitoring,
                    hasNotificationPermission = hasNotificationPermission,
                    displayPreferences = displayPreferences,
                    onRequestNotificationPermission = { requestNotificationPermission() },
                    onToggleMonitoring = { toggleMonitoring() },
                    onRefresh = { refreshBatteryState() },
                    onUpdatePreferences = { updateDisplayPreferences(it) }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        displayPreferences = BatteryPreferences.getPreferences(this)
        registerBatteryReceiver()
        refreshBatteryState()
    }

    override fun onPause() {
        super.onPause()
        unregisterBatteryReceiver()
    }

    private fun registerBatteryReceiver() {
        if (!isReceiverRegistered) {
            val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            registerReceiver(batteryReceiver, filter)
            isReceiverRegistered = true
        }
    }

    private fun unregisterBatteryReceiver() {
        if (isReceiverRegistered) {
            try {
                unregisterReceiver(batteryReceiver)
            } catch (_: Exception) {}
            isReceiverRegistered = false
        }
    }

    private fun checkNotificationPermission() {
        hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            hasNotificationPermission = true
        }
    }

    private fun refreshBatteryState() {
        batteryData = BatteryInfoHelper.getBatteryData(this)
        isMonitoring = BatteryMonitorService.isMonitoringActive(this)
    }

    private fun toggleMonitoring() {
        if (!hasNotificationPermission) {
            requestNotificationPermission()
            return
        }

        if (isMonitoring) {
            stopMonitoringService()
        } else {
            startMonitoringService()
        }
    }

    private fun startMonitoringService() {
        try {
            val intent = Intent(this, BatteryMonitorService::class.java).apply {
                action = BatteryMonitorService.ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
            isMonitoring = true
            Toast.makeText(
                this,
                "Monitoring active! Temperature is now visible in the status bar.",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to start service: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopMonitoringService() {
        try {
            val intent = Intent(this, BatteryMonitorService::class.java).apply {
                action = BatteryMonitorService.ACTION_STOP
            }
            startService(intent)
            isMonitoring = false
            Toast.makeText(this, "Monitoring stopped.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to stop service: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateDisplayPreferences(newPrefs: DisplayPreferences) {
        displayPreferences = newPrefs
        BatteryPreferences.savePreferences(this, newPrefs)
        if (isMonitoring) {
            try {
                val intent = Intent(this, BatteryMonitorService::class.java).apply {
                    action = BatteryMonitorService.ACTION_UPDATE_NOTIFICATION
                }
                startService(intent)
            } catch (e: Exception) {
                // Service may not be actively running
            }
        }
    }
}
