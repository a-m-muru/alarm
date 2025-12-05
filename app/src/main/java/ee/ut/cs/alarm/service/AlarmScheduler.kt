package ee.ut.cs.alarm.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import ee.ut.cs.alarm.data.Alarm
import java.util.Calendar
import java.util.UUID

open class AlarmScheduler(
    private val context: Context,
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * @param day The day of the week to convert. Starts from Monday (1)
     * @return The converted day of the week.
     */
    open fun intToCalendarDay(day: Int): Int {
        // Calendar starts counting days from SUNDAY (1) and ends with SATURDAY (7).
        return day % 7 + 1
    }

    /**
     * @param day The day of the week to convert. Starts from Monday (1)
     * @return The converted day of the week.
     */
    open fun calendarDayToInt(day: Int): Int {
        // Calendar starts counting days from SUNDAY (1) and ends with SATURDAY (7).
        return (day + 5) % 7 + 1
    }

    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            // For older versions, this permission is not needed, so we can consider it "granted"
            true
        }
    }
    fun scheduleAlarm(alarm: Alarm) {
        if (!alarm.enabled) {
            cancelAlarm(alarm.id)
            return
        }

        if (!canScheduleExactAlarms()) {
            return
        }

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, (alarm.time / 3600u).toInt())
        calendar.set(Calendar.MINUTE, ((alarm.time / 60u) % 60u).toInt())
        calendar.set(Calendar.SECOND, (alarm.time % 60u).toInt()) // ?? User can't set seconds for alarm
        calendar.set(Calendar.MILLISECOND, 0)

        if (calendar.timeInMillis <= System.currentTimeMillis())
            calendar.add(Calendar.DATE, 1)

        val daysMask = alarm.days.toInt()

        // if no days are set, schedule for today or tomorrow if time has already passed today
        if (daysMask == 0) {
            setAlarm(calendar, alarm)
            return
        }

        // Set repeating alarm
        val day = calendarDayToInt(calendar.get(Calendar.DAY_OF_WEEK)) - 2 // Sub 2 so Monaday shifts by 0
        // Find the first time the alarm needs to ring
        for (i in 1..7) {
            if (daysMask shr ((i + day) % 7) and 1 > 0) {
                setAlarm(calendar, alarm)
                return
            }
            calendar.add(Calendar.DATE, 1)
        }
    }

    fun setAlarm(time: Calendar, alarm: Alarm) {
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra("ut.cs.alarm.alarm", alarm)
        val pending =
            PendingIntent.getBroadcast(
                context,
                alarm.id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(time.timeInMillis, null),
            pending,
        )
    }

    fun cancelAlarm(id: UUID) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        alarmManager.cancel(pendingIntent)
    }
}
