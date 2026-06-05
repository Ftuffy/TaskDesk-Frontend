package com.example.manajementugas.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.example.manajementugas.Task
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Calendar

class DailyReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prefs = context.getSharedPreferences("TaskPrefs", Context.MODE_PRIVATE)
        val tasks = loadTasks(prefs)

        val now          = Calendar.getInstance().timeInMillis
        val twoDaysLater = now + (2 * 24 * 60 * 60 * 1000L)

        val deadlineSoonCount = tasks.count { task ->
            task.dueDate in now..twoDaysLater
        }

        NotificationHelper.showDailySummaryNotification(
            context           = context,
            totalTasks        = tasks.size,
            deadlineSoonCount = deadlineSoonCount
        )
    }

    private fun loadTasks(prefs: SharedPreferences): List<Task> {
        val json = prefs.getString("taskList", null) ?: return emptyList()
        val type = object : TypeToken<List<Task>>() {}.type
        return Gson().fromJson(json, type)
    }
}