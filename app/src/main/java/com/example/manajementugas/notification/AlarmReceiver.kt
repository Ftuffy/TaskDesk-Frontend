package com.example.manajementugas.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Ambil data dari intent yang dikirim AlarmScheduler
        val taskId    = intent.getLongExtra("TASK_ID", -1L)
        val taskTitle = intent.getStringExtra("TASK_TITLE") ?: "Tugas"
        val isToday   = intent.getBooleanExtra("IS_TODAY", false)

        if (taskId == -1L) return

        // Tampilkan notifikasi dengan suara + getar
        NotificationHelper.showDeadlineNotification(
            context  = context,
            notifId  = taskId.toInt(),
            taskTitle = taskTitle,
            isToday  = isToday
        )
    }
}
