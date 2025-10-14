package ee.ut.cs.alarm.data.repo

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import ee.ut.cs.alarm.data.Alarm
import ee.ut.cs.alarm.data.proto.AlarmListProto;
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

object AlarmListSerializer : Serializer<AlarmListProto> {
    override val defaultValue: AlarmListProto = AlarmListProto.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): AlarmListProto {
        return AlarmListProto.parseFrom(input)
    }

    override suspend fun writeTo(
        t: AlarmListProto,
        output: OutputStream
    ) {
        t.writeTo(output)
    }

}

interface AlarmRepository {
    suspend fun saveAlarm(alarm: Alarm);
    suspend fun deleteAlarm(alarm: Alarm);
    suspend fun getAlarmByID(alarmID: UUID): Alarm?;
}

class AlarmRepositoryImpl private constructor(private val context: Context) : AlarmRepository {
    private val Context.alarmDataStore: DataStore<AlarmListProto> by dataStore(
        fileName = "alarms.pb",
        serializer = AlarmListSerializer
    )

    val alarms: Flow<List<Alarm>> = context.alarmDataStore.data
        .catch { exception -> throw exception }
        .map { alarmList ->
            alarmList.alarmsList.map { Alarm.fromProto(it) }
        }

    override suspend fun saveAlarm(alarm: Alarm) {
        context.alarmDataStore.updateData { currentList ->
            val existingAlarms = currentList.alarmsList.toMutableList()
            val existingIndex = existingAlarms.indexOfFirst { it.id == alarm.id.toString() }

            if (existingIndex >= 0)
                existingAlarms[existingIndex] = alarm.toProto()
            else
                existingAlarms.add(alarm.toProto())

            currentList.toBuilder()
                .clearAlarms()
                .addAllAlarms(existingAlarms)
                .build()
        }
    }

    override suspend fun deleteAlarm(alarm: Alarm) {
        context.alarmDataStore.updateData { currentList ->
            currentList.toBuilder()
                .clearAlarms()
                .addAllAlarms(
                    currentList.alarmsList.filter { it.id != alarm.id.toString() }
                )
                .build()
        }
    }

    override suspend fun getAlarmByID(alarmID: UUID): Alarm? {
        return context.alarmDataStore.data.map { alarmList ->
            alarmList.alarmsList.find { it.id == alarmID.toString() }?.let { Alarm.fromProto(it) }
        }.catch { null }.let {
            null
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: AlarmRepository? = null;

        fun getInstance(context: Context): AlarmRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AlarmRepositoryImpl(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}