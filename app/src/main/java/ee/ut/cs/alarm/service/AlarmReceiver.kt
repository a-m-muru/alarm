package ee.ut.cs.alarm.service

import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import ee.ut.cs.alarm.AlarmActivity
import ee.ut.cs.alarm.data.Alarm
import kotlin.random.Random

class AlarmReceiver : BroadcastReceiver() {
    // my heartfelt gratitude to CHAT GEPT
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val alarm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("ut.cs.alarm.alarm", Alarm::class.java)
        } else {
            intent.getParcelableExtra<Alarm>("ut.cs.alarm.alarm")
        }
        if (alarm == null) {
            Log.e("ALARM RECEIVER", "alarm was null!! stopping anything")
            return
        }

        Log.i("ALARM RECEIVER", "packing up and sending $alarm")

        // Schedule the next alarm
        if (alarm.days > 0) {
            val alarmScheduler = AlarmScheduler(context)
            alarmScheduler.scheduleAlarm(alarm)
        }


        val alarmIntent = Intent(context, AlarmActivity::class.java)
        alarmIntent.putExtra("ut.cs.alarm.alarm", alarm)
        alarmIntent.putExtra(
            "ut.cs.alarm.minigameId",
            Random.nextInt(AlarmActivity.MAX_MINIGAMES),
        )
        Log.d(
            "ALARM RECEIVER",
            "set intent extra to " + alarmIntent.getIntExtra("ut.cs.alarm.minigameId", -2),
        )
        alarmIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP

        val serviceIntent = Intent(context, AlarmForegroundService::class.java)
//        serviceIntent.putExtra("ut.cs.alarm.alarmIntent", alarmIntent)
        serviceIntent.putExtra("ut.cs.alarm.alarm", alarm)
        serviceIntent.putExtra(
            "ut.cs.alarm.minigameId",
            Random.nextInt(AlarmActivity.MAX_MINIGAMES),
        )
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
