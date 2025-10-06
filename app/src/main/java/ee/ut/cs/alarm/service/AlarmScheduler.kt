package ee.ut.cs.alarm.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import ee.ut.cs.alarm.data.Alarm
import ee.ut.cs.alarm.data.DayOfWeek
import java.util.*

class AlarmScheduler(private val context: Context) {
    
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleAlarm(alarm: Alarm) {
        if (!alarm.enabled) {
            cancelAlarm(alarm.id)
            return
        }

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, alarm.hour)
        calendar.set(Calendar.MINUTE, alarm.minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        // Schedule for each selected day
        alarm.days.forEach { day ->
            val dayCalendar = calendar.clone() as Calendar
            dayCalendar.set(Calendar.DAY_OF_WEEK, day.value)
            
            // If the time has already passed today, schedule for next week
            if (dayCalendar.timeInMillis <= System.currentTimeMillis()) {
                dayCalendar.add(Calendar.WEEK_OF_YEAR, 1)
            }

            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("alarm_id", alarm.id)
                putExtra("alarm_hour", alarm.hour)
                putExtra("alarm_minute", alarm.minute)
                putExtra("alarm_label", alarm.label)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                alarm.id.hashCode() + day.value, // Unique request code
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    dayCalendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    dayCalendar.timeInMillis,
                    pendingIntent
                )
            }
        }
    }

    fun cancelAlarm(alarmId: String) {
        // Cancel all pending intents for this alarm
        DayOfWeek.values().forEach { day ->
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                alarmId.hashCode() + day.value,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
    }

    fun rescheduleAllAlarms(alarms: List<Alarm>) {
        // Cancel all existing alarms first
        alarms.forEach { alarm ->
            cancelAlarm(alarm.id)
        }
        
        // Schedule all enabled alarms
        alarms.filter { it.enabled }.forEach { alarm ->
            scheduleAlarm(alarm)
        }
    }
}
