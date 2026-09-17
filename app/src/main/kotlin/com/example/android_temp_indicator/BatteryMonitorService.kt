package com.example.android_temp_indicator

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import java.util.Locale
import kotlin.math.abs

class BatteryMonitorService : Service() {

    companion object {
        const val CHANNEL_ID = "battery_temp_monitor_channel_v2"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "com.example.android_temp_indicator.ACTION_START"
        const val ACTION_STOP = "com.example.android_temp_indicator.ACTION_STOP"
        const val ACTION_UPDATE_NOTIFICATION = "com.example.android_temp_indicator.ACTION_UPDATE_NOTIFICATION"

        private const val PREFS_NAME = "battery_monitor_prefs"
        private const val KEY_IS_ACTIVE = "is_monitoring_active"

        @Volatile
        var isServiceRunning = false
            private set

        fun isMonitoringActive(context: Context): Boolean {
            return isServiceRunning || context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_IS_ACTIVE, false)
        }

        private fun setMonitoringActive(context: Context, active: Boolean) {
            isServiceRunning = active
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_IS_ACTIVE, active)
                .apply()
        }
    }

    private var notificationManager: NotificationManager? = null
    private var isReceiverRegistered = false
    private var lastReportedTemp: Double = -999.0
    private var lastReportedPercentage: Int = -1
    private var lastReportedCharging: Boolean = false

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                val batteryData = BatteryInfoHelper.parseBatteryIntent(intent)
                updateNotificationIfChanged(batteryData)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
        registerBatteryReceiver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopMonitoringService()
            return START_NOT_STICKY
        }

        if (intent?.action == ACTION_UPDATE_NOTIFICATION) {
            if (isMonitoringActive(this)) {
                val currentBattery = BatteryInfoHelper.getBatteryData(this)
                val updatedNotification = buildNotification(currentBattery)
                notificationManager?.notify(NOTIFICATION_ID, updatedNotification)
            }
            return START_STICKY
        }

        setMonitoringActive(this, true)

        val currentBattery = BatteryInfoHelper.getBatteryData(this)
        val initialNotification = buildNotification(currentBattery)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                initialNotification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, initialNotification)
        }

        return START_STICKY
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
            } catch (e: Exception) {
                // Receiver was already unregistered
            }
            isReceiverRegistered = false
        }
    }

    private fun updateNotificationIfChanged(data: BatteryData) {
        // Only update notification when temperature, percentage, or charging state changes
        val tempDiff = abs(data.temperature - lastReportedTemp)
        val isDifferent = tempDiff >= 0.1 ||
                data.percentage != lastReportedPercentage ||
                data.isCharging != lastReportedCharging

        if (isDifferent) {
            lastReportedTemp = data.temperature
            lastReportedPercentage = data.percentage
            lastReportedCharging = data.isCharging

            val notification = buildNotification(data)
            notificationManager?.notify(NOTIFICATION_ID, notification)
        }
    }

    private fun buildNotification(data: BatteryData): Notification {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }

        // Generate dynamic temperature small icon
        try {
            val icon = TemperatureIconGenerator.createTemperatureIcon(this, data.temperature)
            builder.setSmallIcon(icon)
        } catch (e: Exception) {
            // Fallback to vector icon if dynamic generation fails
            builder.setSmallIcon(R.drawable.ic_stat_temp_default)
        }

        val tempStr = String.format(Locale.US, "%.1f°C", data.temperature)

        val prefs = BatteryPreferences.getPreferences(this)
        val items = mutableListOf<String>()
        if (prefs.showBatteryLevel) {
            items.add("Level: ${data.percentage}%")
        }
        if (prefs.showChargingStatus) {
            val chargingStatus = if (data.isCharging) data.chargingType ?: "Charging" else "Discharging"
            items.add(chargingStatus)
        }
        if (prefs.showVoltage) {
            data.voltage?.let {
                items.add(String.format(Locale.US, "%.2fV", it))
            }
        }
        val contentText = if (items.isNotEmpty()) {
            items.joinToString(" • ")
        } else {
            null
        }

        val deleteIntent = Intent(this, BatteryMonitorDismissReceiver::class.java)
        val pendingDeleteIntent = PendingIntent.getBroadcast(
            this,
            0,
            deleteIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        builder.apply {
            setContentTitle("🌡 Battery: $tempStr")
            setContentText(contentText)
            setContentIntent(pendingIntent)
            setDeleteIntent(pendingDeleteIntent)
            setOngoing(true)
            setAutoCancel(false)
            setOnlyAlertOnce(true)
            setVisibility(Notification.VISIBILITY_PUBLIC)
            @Suppress("DEPRECATION")
            setPriority(Notification.PRIORITY_MAX)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                setCategory(Notification.CATEGORY_STATUS)
            }
        }

        val notification = builder.build()
        // Lock notification with ongoing flags to prevent user swiping it away
        notification.flags = notification.flags or
                Notification.FLAG_ONGOING_EVENT or
                Notification.FLAG_NO_CLEAR

        return notification
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Remove legacy low-priority channel if present
            try {
                notificationManager?.deleteNotificationChannel("battery_temp_monitor_channel")
            } catch (e: Exception) {
                // Ignore
            }

            val channel = NotificationChannel(
                CHANNEL_ID,
                "Battery Temperature Monitor",
                NotificationManager.IMPORTANCE_HIGH // High priority: prioritizes notification to the top of shade
            ).apply {
                description = "Shows live battery temperature at the top of the notification shade"
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
                setSound(null, null)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun stopMonitoringService() {
        setMonitoringActive(this, false)
        unregisterBatteryReceiver()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }

    override fun onDestroy() {
        setMonitoringActive(this, false)
        unregisterBatteryReceiver()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
