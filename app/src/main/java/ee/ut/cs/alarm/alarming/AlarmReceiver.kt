package ee.ut.cs.alarm.alarming

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import ee.ut.cs.alarm.AlarmActivity
import ee.ut.cs.alarm.R

class AlarmReceiver : BroadcastReceiver() {
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onReceive(context: Context, intent: Intent?) {
        val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        context.startActivity(alarmIntent)
        showAlarmNotification(context, "asaskasldasd")
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showAlarmNotification(context: Context, label: String?) {
        val title = label ?: "Alarmmmmm"

        val notification = NotificationCompat.Builder(context, "alarm_channel")
            .setContentTitle(title)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(false)
            .setOngoing(true)
            .build()

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(1, notification)
    }
}