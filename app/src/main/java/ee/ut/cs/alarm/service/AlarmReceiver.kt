package ee.ut.cs.alarm.service

import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import ee.ut.cs.alarm.AlarmActivity
import ee.ut.cs.alarm.data.Alarm

class AlarmReceiver : BroadcastReceiver() {
    // my heartfelt gratitude to CHAT GEPT
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

        val alarmIntent = Intent(context, AlarmActivity::class.java)
        alarmIntent.putExtra("alarm", alarm)
        alarmIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP

        val serviceIntent = Intent(context, AlarmForegroundService::class.java)
        serviceIntent.putExtra("alarmIntent", alarmIntent)
        ContextCompat.startForegroundService(context, serviceIntent)

        if (canStartActivity(context)) {
            context.startActivity(alarmIntent)
        }
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
