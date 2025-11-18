package ee.ut.cs.alarm.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import ee.ut.cs.alarm.data.Alarm
import java.util.Calendar
import java.util.UUID

class AlarmScheduler(
    private val context: Context,
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleAlarm(alarm: Alarm) {
        if (!alarm.enabled) {
            cancelAlarm(alarm.id)
            return
        }

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, (alarm.time / 3600u).toInt())
        calendar.set(Calendar.MINUTE, ((alarm.time / 60u) % 60u).toInt())
        calendar.set(Calendar.SECOND, (alarm.time % 60u).toInt())
        calendar.set(Calendar.MILLISECOND, 0)

        var daysMask = alarm.days.toInt()
        for (i in 1..7) {
            if (daysMask and 1 > 0) {
                val dayCalendar = calendar.clone() as Calendar
                when (i) {
                    1 -> dayCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                    2 -> dayCalendar.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY)
                    3 -> dayCalendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY)
                    4 -> dayCalendar.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY)
                    5 -> dayCalendar.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY)
                    6 -> dayCalendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
                    7 -> dayCalendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
                }

                // If the time has already passed today, schedule for next week
                if (dayCalendar.timeInMillis <= System.currentTimeMillis()) {
                    dayCalendar.add(Calendar.WEEK_OF_YEAR, 1)
                }

                val intent =
                    Intent(context, AlarmReceiver::class.java)
                        .apply {
                            putExtra("alarm", alarm)
                        }.let { intent ->
                            PendingIntent.getBroadcast(
                                context,
                                alarm.id.hashCode() + i,
                                intent,
                                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                            )
                        }

                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(dayCalendar.timeInMillis, null),
                    intent,
                )
            }

            daysMask = daysMask shr 1
        }
    }

    fun cancelAlarm(id: UUID) {
        for (i in 1..7) {
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent =
                PendingIntent.getBroadcast(
                    context,
                    id.hashCode() + i,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
            alarmManager.cancel(pendingIntent)
        }
    }
}
