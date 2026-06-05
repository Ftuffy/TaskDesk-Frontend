package com.example.manajementugas.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.manajementugas.Task
import java.util.Calendar

object AlarmScheduler {

    fun scheduleTaskAlarm(context: Context, task: Task) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // ⚠️ DEMO MODE — notifikasi muncul 10 detik setelah task dibuat
        // Ganti ke kode H-1 & H-0 setelah demo selesai!
        val demoTime = System.currentTimeMillis() + 10_000L

        // Notifikasi pertama — 10 detik (simulasi H-1)
        val intentH1 = createAlarmIntent(
            context     = context,
            taskId      = task.id,
            taskTitle   = task.title,
            isToday     = false,
            requestCode = task.id.toInt()
        )
        setExactAlarm(alarmManager, demoTime, intentH1, context)

        // Notifikasi kedua — 20 detik (simulasi H-0)
        val intentH0 = createAlarmIntent(
            context     = context,
            taskId      = task.id,
            taskTitle   = task.title,
            isToday     = true,
            requestCode = task.id.toInt() + 10000
        )
        setExactAlarm(alarmManager, demoTime + 10_000L, intentH0, context)

        /* ── KODE ASLI (aktifkan kembali setelah demo) ──────────
        // Alarm H-1 jam 8 pagi
        val oneDayBefore = Calendar.getInstance().apply {
            timeInMillis = task.dueDate
            add(Calendar.DAY_OF_MONTH, -1)
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (oneDayBefore.timeInMillis > System.currentTimeMillis()) {
            val intentH1 = createAlarmIntent(
                context     = context,
                taskId      = task.id,
                taskTitle   = task.title,
                isToday     = false,
                requestCode = task.id.toInt()
            )
            setExactAlarm(alarmManager, oneDayBefore.timeInMillis, intentH1, context)
        }

        // Alarm H-0 jam 8 pagi
        val onDeadlineDay = Calendar.getInstance().apply {
            timeInMillis = task.dueDate
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (onDeadlineDay.timeInMillis > System.currentTimeMillis()) {
            val intentH0 = createAlarmIntent(
                context     = context,
                taskId      = task.id,
                taskTitle   = task.title,
                isToday     = true,
                requestCode = task.id.toInt() + 10000
            )
            setExactAlarm(alarmManager, onDeadlineDay.timeInMillis, intentH0, context)
        }
        ── akhir kode asli ── */
    }

    fun cancelTaskAlarm(context: Context, task: Task) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intentH1 = createAlarmIntent(
            context     = context,
            taskId      = task.id,
            taskTitle   = task.title,
            isToday     = false,
            requestCode = task.id.toInt()
        )
        alarmManager.cancel(intentH1)

        val intentH0 = createAlarmIntent(
            context     = context,
            taskId      = task.id,
            taskTitle   = task.title,
            isToday     = true,
            requestCode = task.id.toInt() + 10000
        )
        alarmManager.cancel(intentH0)
    }

    fun scheduleDailyReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, DailyReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 8888, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    private fun createAlarmIntent(
        context: Context,
        taskId: Long,
        taskTitle: String,
        isToday: Boolean,
        requestCode: Int
    ): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("TASK_ID",    taskId)
            putExtra("TASK_TITLE", taskTitle)
            putExtra("IS_TODAY",   isToday)
        }
        return PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun setExactAlarm(
        alarmManager: AlarmManager,
        triggerTime: Long,
        pendingIntent: PendingIntent,
        context: Context
    ) {
        // ✅ Handle canScheduleExactAlarms untuk Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                // Fallback ke inexact alarm jika izin tidak diberikan
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }
}