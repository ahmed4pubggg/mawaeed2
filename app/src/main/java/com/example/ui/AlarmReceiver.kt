package com.example.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val appointmentText = intent.getStringExtra("appointment_text") ?: "حصة قرآنية"
        val appointmentDay = intent.getStringExtra("appointment_day") ?: ""
        val appointmentTime = intent.getStringExtra("appointment_time") ?: ""
        val ringtoneUri = intent.getStringExtra("ringtone_uri") ?: ""

        // Acquire a WakeLock to keep the CPU awake while starting the activity
        try {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? android.os.PowerManager
            val wakeLock = powerManager?.newWakeLock(
                android.os.PowerManager.PARTIAL_WAKE_LOCK,
                "QuranApp::AlarmWakeLock"
            )
            wakeLock?.acquire(10000L /* 10 seconds */)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Launch full-screen alarm activity
        val activityIntent = Intent(context, AlarmActivity::class.java).apply {
            putExtra("appointment_text", appointmentText)
            putExtra("appointment_day", appointmentDay)
            putExtra("appointment_time", appointmentTime)
            putExtra("ringtone_uri", ringtoneUri)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        
        try {
            context.startActivity(activityIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Send high-priority notification as a reliable fallback/companion
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "appointments_alarm_channel"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "تنبيهات المواعيد",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "تنبيهات الحصص والمواعيد المثبتة"
                enableLights(true)
                lightColor = Color.GREEN
                enableVibration(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                setBypassDnd(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("حان موعد الحصة القرآنية ⏰")
            .setContentText("$appointmentDay - الساعة $appointmentTime: $appointmentText")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(pendingIntent, true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(999, notification)
    }
}
