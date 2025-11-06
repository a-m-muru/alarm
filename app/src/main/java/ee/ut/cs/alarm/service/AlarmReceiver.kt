package ee.ut.cs.alarm.service

import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import ee.ut.cs.alarm.AlarmActivity
import ee.ut.cs.alarm.R
import ee.ut.cs.alarm.data.Alarm

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val alarm = intent.getParcelableExtra<Alarm>("alarm")
        if (alarm == null) {
            Log.e("ALARM RECEIVER", "alarm was null!! stopping anything")
            return
        }

        Log.i("ALARM RECEIVER", "packing up and sending $alarm")

        if (canStartActivity(context)) {
            val alarmIntent =
                Intent(context, AlarmActivity::class.java).apply {
                    putExtra("alarm", alarm)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }

            context.startActivity(alarmIntent)
        } else {
            sendNotification(context)
        }
        // Start AlarmActivity
    }

    // thanx CHATGPT 💝💝💝💝💝💝💝
    fun sendNotification(ctx: Context) {
        val channelID = "ALARM CHANNEL ALARM++ COOL CHANNEL"
        val channelName = "Alarm++ Notifications"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notifManager = ctx.getSystemService(NotificationManager::class.java)
            var channel = notifManager.getNotificationChannel(channelID)

            if (channel == null) {
                channel =
                    NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH)
                notifManager.createNotificationChannel(channel)
            }
        }

        val notifBuilder =
            NotificationCompat
                .Builder(ctx, channelID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("WAKE nice UP!!!!!!!!!!")
                .setContentText(":) pls wake up \uD83D\uDC9D \uD83D\uDC9D \uD83D\uDC9D")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(false)

        val notifManager = ctx.getSystemService(NotificationManager::class.java)
        notifManager.notify(1, notifBuilder.build())
    }

    // thanx CHATGPT 💝💝💝💝💝💝💝
    private fun canStartActivity(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningAppProcesses = activityManager.getRunningAppProcesses()

        if (runningAppProcesses != null) {
            for (processInfo in runningAppProcesses) {
                // Check if our app is in the foreground
                if (processInfo.processName == context.getPackageName() &&
                    processInfo.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                ) {
                    return true // App is in the foreground
                }
            }
        }
        return false // App is not in the foreground
    }
}
