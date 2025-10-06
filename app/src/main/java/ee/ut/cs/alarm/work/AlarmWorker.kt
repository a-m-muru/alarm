package ee.ut.cs.alarm.work

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ee.ut.cs.alarm.service.AlarmActivity
import ee.ut.cs.alarm.service.AlarmService

class AlarmWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val tag = "AlarmWorker"

    override suspend fun doWork(): Result {
        Log.d(tag, "AlarmWorker doWork() called")
        
        val alarmId = inputData.getString("alarm_id") ?: return Result.failure()
        val alarmHour = inputData.getInt("alarm_hour", 0)
        val alarmMinute = inputData.getInt("alarm_minute", 0)
        val alarmLabel = inputData.getString("alarm_label")

        Log.d(tag, "Alarm triggered: $alarmId at $alarmHour:$alarmMinute")

        // Start alarm service for sound and vibration
        val serviceIntent = Intent(applicationContext, AlarmService::class.java).apply {
            action = "START_ALARM"
            putExtra("alarm_id", alarmId)
            putExtra("alarm_hour", alarmHour)
            putExtra("alarm_minute", alarmMinute)
            putExtra("alarm_label", alarmLabel)
        }
        Log.d(tag, "Starting AlarmService")
        applicationContext.startService(serviceIntent)

        // Start alarm activity
        val alarmIntent = Intent(applicationContext, AlarmActivity::class.java).apply {
            putExtra("alarm_id", alarmId)
            putExtra("alarm_hour", alarmHour)
            putExtra("alarm_minute", alarmMinute)
            putExtra("alarm_label", alarmLabel)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                   Intent.FLAG_ACTIVITY_CLEAR_TOP or 
                   Intent.FLAG_ACTIVITY_SINGLE_TOP or
                   Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
        }
        
        Log.d(tag, "Starting AlarmActivity")
        applicationContext.startActivity(alarmIntent)

        Log.d(tag, "AlarmWorker completed successfully")
        return Result.success()
    }
}
