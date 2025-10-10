package ee.ut.cs.alarm.alarming

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import ee.ut.cs.alarm.data.Alarm

class AlarmScheduler(private val ctx: Context) {
    private val alarmManager = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleAlarm(alarm: Alarm) {
        val calendar = Calendar.getInstance()

        val intent = Intent(ctx, AlarmReceiver::class.java).apply {

        }

        val pendingIntent = PendingIntent.getBroadcast(
            ctx,
            6767,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + 1000,
            pendingIntent,
        )
    }
}