package com.falahpro.app.prayer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.falahpro.app.core.util.PrayerReliabilityHelper

@Composable
fun PrayerReliabilityBanner() {
    val context = LocalContext.current
    val exactAlarms = PrayerReliabilityHelper.canScheduleExactAlarms(context)
    val notifications = PrayerReliabilityHelper.areNotificationsEnabled(context)
    val batteryOk = PrayerReliabilityHelper.isIgnoringBatteryOptimizations(context)
    val oem = PrayerReliabilityHelper.detectOem()
    val oemGuidance = PrayerReliabilityHelper.getOemBatteryGuidance(oem)

    if (exactAlarms && notifications && batteryOk) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF3E2A24), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text("Prayer alerts need attention", color = Color(0xFFE2C07A), fontSize = 13.sp)
        Spacer(Modifier.height(6.dp))

        if (!exactAlarms) {
            Text(
                "• Allow Alarms & reminders for exact prayer times",
                color = Color.White.copy(0.85f),
                fontSize = 12.sp,
                modifier = Modifier
                    .clickable { PrayerReliabilityHelper.openExactAlarmSettings(context) }
                    .padding(vertical = 2.dp)
            )
        }
        if (!notifications) {
            Text(
                "• Enable notifications for prayer alerts",
                color = Color.White.copy(0.85f),
                fontSize = 12.sp,
                modifier = Modifier
                    .clickable { PrayerReliabilityHelper.openNotificationSettings(context) }
                    .padding(vertical = 2.dp)
            )
        }
        if (!batteryOk) {
            Text(
                "• Disable battery optimization for Falah Pro",
                color = Color.White.copy(0.85f),
                fontSize = 12.sp,
                modifier = Modifier
                    .clickable { PrayerReliabilityHelper.requestIgnoreBatteryOptimizations(context) }
                    .padding(vertical = 2.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(oemGuidance, color = Color.White.copy(0.6f), fontSize = 11.sp)
        }
    }
}
