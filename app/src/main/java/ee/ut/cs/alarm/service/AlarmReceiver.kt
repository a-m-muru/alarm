package ee.ut.cs.alarm.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ee.ut.cs.alarm.AlarmActivity
import ee.ut.cs.alarm.data.Alarm

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarm = intent.getParcelableExtra<Alarm>("alarm") ?: return

        // Start AlarmActivity
        val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
            putExtra("alarm", alarm)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        context.startActivity(alarmIntent)
    }
}