package ee.ut.cs.alarm.ui.helpers

import ee.ut.cs.alarm.data.Alarm
import ee.ut.cs.alarm.data.repo.AlarmRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class TestAlarmRepository : AlarmRepository {
    private val alarms = mutableListOf<Alarm>()
    private val alarmsFlow = MutableStateFlow<List<Alarm>>(emptyList())

    override suspend fun getAlarms(): Flow<List<Alarm>> = alarmsFlow.asStateFlow()

    override suspend fun saveAlarm(alarm: Alarm) {
        val existingIndex = alarms.indexOfFirst { it.id == alarm.id }
        if (existingIndex >= 0) {
            alarms[existingIndex] = alarm
        } else {
            alarms.add(alarm)
        }
        emitSnapshot()
    }

    override suspend fun deleteAlarm(alarm: Alarm) {
        alarms.removeAll { it.id == alarm.id }
        emitSnapshot()
    }

    override suspend fun getAlarmByID(alarmID: UUID): Alarm? {
        return alarms.find { it.id == alarmID }
    }

    private fun emitSnapshot() {
        alarmsFlow.value = alarms.toList()
    }
}

