package ee.ut.cs.alarm.ui.helpers

import android.content.Context
import ee.ut.cs.alarm.data.Alarm
import ee.ut.cs.alarm.service.AlarmScheduler
import java.util.UUID

class TestAlarmScheduler(
    context: Context
) : AlarmScheduler(context) {
    private val scheduledAlarms = mutableListOf<Alarm>()

    override fun canScheduleExactAlarms(): Boolean = true

    override fun scheduleAlarm(alarm: Alarm) {
        scheduledAlarms.removeAll { it.id == alarm.id }
        scheduledAlarms.add(alarm)
    }

    override fun cancelAlarm(id: UUID) {
        scheduledAlarms.removeAll { it.id == id }
    }

    fun getScheduledAlarms(): List<Alarm> = scheduledAlarms.toList()
}

