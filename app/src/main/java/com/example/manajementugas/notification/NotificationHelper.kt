package com.example.manajementugas.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.example.manajementugas.MainActivity
import com.example.manajementugas.R

object NotificationHelper {

    const val CHANNEL_DEADLINE = "channel_deadline"
    const val CHANNEL_DAILY    = "channel_daily"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val deadlineChannel = NotificationChannel(
                CHANNEL_DEADLINE,
                "Pengingat Deadline",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description     = "Notifikasi pengingat deadline tugas"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                setSound(soundUri, audioAttributes)
                enableLights(true)
                lightColor = android.graphics.Color.parseColor("#5271FF")
            }

            val dailyChannel = NotificationChannel(
                CHANNEL_DAILY,
                "Ringkasan Harian",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description     = "Notifikasi ringkasan tugas setiap pagi"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300)
                setSound(soundUri, audioAttributes)
            }

            notificationManager.createNotificationChannel(deadlineChannel)
            notificationManager.createNotificationChannel(dailyChannel)
        }
    }

    fun showDeadlineNotification(
        context: Context,
        notifId: Int,
        taskTitle: String,
        isToday: Boolean
    ) {
        val notificationManager = context.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, notifId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title   = if (isToday) "⚠️ Deadline Hari Ini!" else "🔔 Deadline Besok!"
        val message = if (isToday)
            "Tugas \"$taskTitle\" harus selesai hari ini!"
        else
            "Tugas \"$taskTitle\" deadline besok. Jangan sampai lupa!"

        val vibrationPattern = longArrayOf(0, 500, 200, 500)

        val notification = NotificationCompat.Builder(context, CHANNEL_DEADLINE)
            .setSmallIcon(R.drawable.ic_task)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(vibrationPattern)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setColor(android.graphics.Color.parseColor("#5271FF"))
            .build()

        notificationManager.notify(notifId, notification)
        triggerVibration(context, vibrationPattern)
    }

    fun showDailySummaryNotification(
        context: Context,
        totalTasks: Int,
        deadlineSoonCount: Int
    ) {
        val notificationManager = context.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 9999, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val message = when {
            totalTasks == 0        -> "Tidak ada tugas hari ini. Tambahkan tugas baru!"
            deadlineSoonCount > 0  -> "Kamu punya $totalTasks tugas. $deadlineSoonCount tugas deadline dalam 2 hari ke depan!"
            else                   -> "Kamu punya $totalTasks tugas hari ini. Semangat! 💪"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_DAILY)
            .setSmallIcon(R.drawable.ic_task)
            .setContentTitle("📋 Ringkasan Tugas Harian")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 300))
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .build()

        notificationManager.notify(9999, notification)
    }

    private fun triggerVibration(context: Context, pattern: LongArray) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(
                Context.VIBRATOR_MANAGER_SERVICE
            ) as VibratorManager
            vibratorManager.defaultVibrator.vibrate(
                VibrationEffect.createWaveform(pattern, -1)
            )
        } else {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(pattern, -1)
            }
        }
    }
}