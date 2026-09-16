package com.example.android_temp_indicator.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.android_temp_indicator.BatteryData
import com.example.android_temp_indicator.TemperatureStatus
import com.example.android_temp_indicator.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    batteryData: BatteryData,
    isMonitoring: Boolean,
    hasNotificationPermission: Boolean,
    onRequestNotificationPermission: () -> Unit,
    onToggleMonitoring: () -> Unit,
    onRefresh: () -> Unit
) {
    var showHyperOSTips by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Battery Temperature",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(
                            Icons.Rounded.Refresh,
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    IconButton(onClick = { showHyperOSTips = true }) {
                        Icon(
                            Icons.Rounded.Info,
                            contentDescription = "HyperOS Tips",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Permission warning if not granted
            if (!hasNotificationPermission) {
                NotificationPermissionWarning(onRequestPermission = onRequestNotificationPermission)
            }

            // Status Indicator Banner
            StatusIndicatorBanner(isMonitoring = isMonitoring)

            // Temperature Hero Card
            TemperatureHeroCard(batteryData = batteryData)

            // Stats Grid 2x2
            StatsGrid(batteryData = batteryData)

            // Monitoring Control Card
            MonitoringControlCard(
                isMonitoring = isMonitoring,
                onToggleMonitoring = onToggleMonitoring
            )

            // Status Bar Preview Card
            StatusBarPreviewCard(batteryData = batteryData)

            // Optimization Guide Card
            OptimizationGuideCard(onViewTipsClick = { showHyperOSTips = true })

            Spacer(modifier = Modifier.height(16.dp))
        }

        if (showHyperOSTips) {
            HyperOSTipsDialog(onDismiss = { showHyperOSTips = false })
        }
    }
}

@Composable
private fun NotificationPermissionWarning(onRequestPermission: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = WarningDark),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Rounded.NotificationsOff,
                contentDescription = null,
                tint = WarningLight
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Notification Permission Required",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    "Required to display the temperature indicator in the status bar.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFFFED7AA),
                        fontSize = 12.sp
                    )
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(
                    containerColor = TempHot,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Grant", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun StatusIndicatorBanner(isMonitoring: Boolean) {
    val statusColor = if (isMonitoring) StatusActive else StatusInactive

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(statusColor.copy(alpha = 0.1f))
            .border(BorderStroke(1.dp, statusColor.copy(alpha = 0.3f)), RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = if (isMonitoring) "Status Bar Monitoring: ACTIVE" else "Status Bar Monitoring: INACTIVE",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = statusColor
                )
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = if (isMonitoring) "Running" else "Stopped",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    color = statusColor.copy(alpha = 0.8f)
                )
            )
        }
    }
}

@Composable
private fun TemperatureHeroCard(batteryData: BatteryData) {
    val status = batteryData.status
    val statusColor = when (status) {
        TemperatureStatus.COOL -> TempCool
        TemperatureStatus.WARM -> TempWarm
        TemperatureStatus.HOT -> TempHot
        TemperatureStatus.VERY_HOT -> TempVeryHot
    }

    val statusIcon = when (status) {
        TemperatureStatus.COOL -> Icons.Rounded.AcUnit
        TemperatureStatus.WARM -> Icons.Rounded.WbSunny
        TemperatureStatus.HOT -> Icons.Rounded.LocalFireDepartment
        TemperatureStatus.VERY_HOT -> Icons.Rounded.WarningAmber
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Slate800),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Slate700),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(vertical = 24.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "BATTERY TEMPERATURE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 12.sp,
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate400
                    )
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(statusColor.copy(alpha = 0.12f))
                        .border(BorderStroke(1.dp, statusColor.copy(alpha = 0.35f)), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            statusIcon,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            status.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.Center
            ) {
                val tempText = if (batteryData.temperature > 0) {
                    String.format(Locale.US, "%.1f", batteryData.temperature)
                } else {
                    "--.-"
                }

                Text(
                    tempText,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 60.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-1.5).sp,
                        color = statusColor
                    )
                )
                Text(
                    "°C",
                    modifier = Modifier.padding(top = 8.dp, start = 4.dp),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate400
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                status.description,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 13.sp,
                    color = Slate400
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun StatsGrid(batteryData: BatteryData) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Battery Level",
                value = "${batteryData.percentage}%",
                subtitle = if (batteryData.isCharging) "Charging" else "On battery",
                icon = if (batteryData.isCharging) Icons.Rounded.BatteryChargingFull else Icons.Rounded.BatteryStd,
                accentColor = Sky400
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Charging Status",
                value = if (batteryData.isCharging) "Charging" else "Not Charging",
                subtitle = batteryData.chargingType ?: "Discharging",
                icon = if (batteryData.isCharging) Icons.Rounded.Bolt else Icons.Rounded.PowerOff,
                accentColor = if (batteryData.isCharging) StatusActive else Slate500
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Voltage",
                value = batteryData.voltage?.let { String.format(Locale.US, "%.2f V", it) } ?: "Unavailable",
                subtitle = "Cell condition",
                icon = Icons.Rounded.ElectricMeter,
                accentColor = VoltagePurple
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Battery Health",
                value = batteryData.health ?: "Unknown",
                subtitle = "Reported by sensor",
                icon = Icons.Rounded.HealthAndSafety,
                accentColor = if (batteryData.health == "Good") StatusActive else TempVeryHot
            )
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Slate800),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Slate700)
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate400
                    )
                )
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Column {
                Text(
                    value,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = Slate500
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun MonitoringControlCard(
    isMonitoring: Boolean,
    onToggleMonitoring: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Slate800),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Slate700),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val iconBg = if (isMonitoring) StatusActive.copy(alpha = 0.15f) else Slate500.copy(alpha = 0.15f)
                val iconTint = if (isMonitoring) StatusActive else Slate400

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isMonitoring) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (isMonitoring) "Foreground Service Active" else "Ready to Monitor",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        if (isMonitoring) {
                            "Temperature is displayed in status bar. App can be closed."
                        } else {
                            "Turn on to display temperature in status bar."
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            color = Slate400
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onToggleMonitoring,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isMonitoring) TempVeryHot else Sky600,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    if (isMonitoring) Icons.Rounded.StopCircle else Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (isMonitoring) "Stop Monitoring" else "Start Monitoring",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
private fun StatusBarPreviewCard(batteryData: BatteryData) {
    val temp = batteryData.temperature.toInt().coerceAtLeast(0)
    val percentage = batteryData.percentage

    Card(
        colors = CardDefaults.cardColors(containerColor = Slate800),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Slate700),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "STATUS BAR PREVIEW",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    letterSpacing = 1.1.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate400
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0B1329))
                    .border(BorderStroke(1.dp, Slate800), RoundedCornerShape(10.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "12:30",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))

                    // Mock status bar temperature badge beside clock
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color.White.copy(alpha = 0.24f))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            "${if (temp > 0) temp else 38}°",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Icon(
                        Icons.Rounded.SignalCellular4Bar,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Rounded.Wifi,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "${if (percentage > 0) percentage else 72}%",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        Icons.Rounded.Battery5Bar,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Temperature indicator appears beside the clock (status bar notification area) via an ongoing notification small icon without requiring overlay permissions.",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    color = Slate500
                )
            )
        }
    }
}

@Composable
private fun OptimizationGuideCard(onViewTipsClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Slate800),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Slate700),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Rounded.Lightbulb,
                contentDescription = null,
                tint = Color(0xFFFBBF24),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    "Notice for Xiaomi / Poco (HyperOS)",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "To ensure monitoring continues with screen off on HyperOS, set Battery Saver to \"No restrictions\" and enable \"Autostart\".",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = Slate400
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "View setup instructions →",
                    modifier = Modifier.clickable { onViewTipsClick() },
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        color = Sky400,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}

@Composable
private fun HyperOSTipsDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Slate800,
        titleContentColor = Color.White,
        textContentColor = Slate400,
        icon = {
            Icon(
                Icons.Rounded.PhoneAndroid,
                contentDescription = null,
                tint = Sky400
            )
        },
        title = {
            Text(
                "Xiaomi / HyperOS Setup Tips",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "HyperOS / MIUI has aggressive battery management. Follow these steps to prevent the system from terminating background monitoring:",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, color = Color.White)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "1. Battery Saver:\nGo to App Info > Battery saver > Select \"No restrictions\".",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, color = Slate400)
                )
                Text(
                    "2. Autostart:\nGo to App Info > Enable \"Autostart\".",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, color = Slate400)
                )
                Text(
                    "3. Lock in Recents:\nOpen Recent Apps > Long-press Battery Monitor > Tap the Lock icon.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, color = Slate400)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Got it", color = Sky400, fontWeight = FontWeight.Bold)
            }
        }
    )
}
