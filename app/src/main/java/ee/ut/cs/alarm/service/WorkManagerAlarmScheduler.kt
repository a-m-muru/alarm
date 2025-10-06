package ee.ut.cs.alarm.service

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.work.*
import ee.ut.cs.alarm.data.Alarm
import ee.ut.cs.alarm.data.DayOfWeek
import ee.ut.cs.alarm.work.AlarmWorker
import java.util.*
import java.util.concurrent.TimeUnit

class WorkManagerAlarmScheduler(private val context: Context) {
    
    private val workManager = WorkManager.getInstance(context)
    private val tag = "WorkManagerAlarmScheduler"

    fun scheduleAlarm(alarm: Alarm) {
        Log.d(tag, "Scheduling alarm: ${alarm.id}, enabled: ${alarm.enabled}")
        Log.d(tag, "Alarm details: ${alarm.hour}:${alarm.minute}, days: ${alarm.days.map { it.displayName }}")
        
        if (!alarm.enabled) {
            Log.d(tag, "Alarm disabled, cancelling: ${alarm.id}")
            cancelAlarm(alarm.id)
            return
        }

        // Cancel existing work for this alarm
        Log.d(tag, "Cancelling existing work for alarm: ${alarm.id}")
        cancelAlarm(alarm.id)

        // Schedule for each selected day
        alarm.days.forEach { day ->
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, alarm.hour)
            calendar.set(Calendar.MINUTE, alarm.minute)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            // Convert our DayOfWeek enum to Calendar.DAY_OF_WEEK
            // Our enum: 1=Monday, 2=Tuesday, ..., 7=Sunday
            // Calendar: 1=Sunday, 2=Monday, ..., 7=Saturday
            val calendarDayOfWeek = when (day) {
                DayOfWeek.MONDAY -> Calendar.MONDAY
                DayOfWeek.TUESDAY -> Calendar.TUESDAY
                DayOfWeek.WEDNESDAY -> Calendar.WEDNESDAY
                DayOfWeek.THURSDAY -> Calendar.THURSDAY
                DayOfWeek.FRIDAY -> Calendar.FRIDAY
                DayOfWeek.SATURDAY -> Calendar.SATURDAY
                DayOfWeek.SUNDAY -> Calendar.SUNDAY
            }
            calendar.set(Calendar.DAY_OF_WEEK, calendarDayOfWeek)
            
            // If the time has already passed today, schedule for next week
            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                Log.d(tag, "Alarm time has passed today, scheduling for next week")
                calendar.add(Calendar.WEEK_OF_YEAR, 1)
            }

            val delayMillis = calendar.timeInMillis - System.currentTimeMillis()
            val delayMinutes = delayMillis / (1000 * 60)
            Log.d(tag, "Scheduling alarm for day ${day.displayName} in ${delayMillis}ms (${delayMinutes} minutes)")
            Log.d(tag, "Target time: ${calendar.get(Calendar.HOUR_OF_DAY)}:${calendar.get(Calendar.MINUTE)}")

            val inputData = Data.Builder()
                .putString("alarm_id", alarm.id)
                .putInt("alarm_hour", alarm.hour)
                .putInt("alarm_minute", alarm.minute)
                .putString("alarm_label", alarm.label ?: "")
                .build()

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .setRequiresBatteryNotLow(false)
                .setRequiresCharging(false)
                .setRequiresDeviceIdle(false)
                .build()

            val alarmWork = OneTimeWorkRequestBuilder<AlarmWorker>()
                .setInputData(inputData)
                .setConstraints(constraints)
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .addTag("alarm_${alarm.id}_${day.value}")
                .build()

            val workName = "alarm_${alarm.id}_${day.value}"
            Log.d(tag, "Enqueuing work: $workName with delay: ${delayMinutes} minutes")
            
            try {
                workManager.enqueueUniqueWork(
                    workName,
                    ExistingWorkPolicy.REPLACE,
                    alarmWork
                )
                Log.d(tag, "Successfully enqueued work: $workName")
            } catch (e: Exception) {
                Log.e(tag, "Failed to enqueue work: $workName", e)
            }
        }
    }

    fun cancelAlarm(alarmId: String) {
        Log.d(tag, "Cancelling alarm: $alarmId")
        // Cancel all work for this alarm across all days
        DayOfWeek.values().forEach { day ->
            val workName = "alarm_${alarmId}_${day.value}"
            workManager.cancelUniqueWork(workName)
            Log.d(tag, "Cancelled work: $workName")
        }
    }

    fun rescheduleAllAlarms(alarms: List<Alarm>) {
        Log.d(tag, "Rescheduling ${alarms.size} alarms")
        // Cancel all existing alarm work
        alarms.forEach { alarm ->
            cancelAlarm(alarm.id)
        }
        
        // Schedule all enabled alarms
        alarms.filter { it.enabled }.forEach { alarm ->
            scheduleAlarm(alarm)
        }
    }
}
