package ee.ut.cs.alarm.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import ee.ut.cs.alarm.proto.AlarmListProto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.InputStream
import java.io.OutputStream

class AlarmRepository private constructor(private val context: Context) {
    
    private val Context.alarmDataStore: DataStore<AlarmListProto> by dataStore(
        fileName = "alarms.pb",
        serializer = AlarmListSerializer
    )

    val alarms: Flow<List<Alarm>> = context.alarmDataStore.data
        .catch { exception ->
            // Handle any errors
            throw exception
        }
        .map { alarmList ->
            alarmList.alarmsList.map { Alarm.fromProto(it) }
        }

    suspend fun saveAlarm(alarm: Alarm) {
        context.alarmDataStore.updateData { currentList ->
            val existingAlarms = currentList.alarmsList.toMutableList()
            val existingIndex = existingAlarms.indexOfFirst { it.id == alarm.id }
            
            if (existingIndex >= 0) {
                existingAlarms[existingIndex] = alarm.toProto()
            } else {
                existingAlarms.add(alarm.toProto())
            }
            
            currentList.toBuilder()
                .clearAlarms()
                .addAllAlarms(existingAlarms)
                .build()
        }
    }

    suspend fun deleteAlarm(alarmId: String) {
        context.alarmDataStore.updateData { currentList ->
            currentList.toBuilder()
                .clearAlarms()
                .addAllAlarms(
                    currentList.alarmsList.filter { it.id != alarmId }
                )
                .build()
        }
    }

    suspend fun updateAlarmEnabled(alarmId: String, enabled: Boolean) {
        context.alarmDataStore.updateData { currentList ->
            val updatedAlarms = currentList.alarmsList.map { alarmProto ->
                if (alarmProto.id == alarmId) {
                    alarmProto.toBuilder().setEnabled(enabled).build()
                } else {
                    alarmProto
                }
            }
            
            currentList.toBuilder()
                .clearAlarms()
                .addAllAlarms(updatedAlarms)
                .build()
        }
    }

    suspend fun getAlarmById(alarmId: String): Alarm? {
        return context.alarmDataStore.data.map { alarmList ->
            alarmList.alarmsList.find { it.id == alarmId }?.let { Alarm.fromProto(it) }
        }.catch { null }.let { flow ->
            // This is a simplified approach - in production you'd want proper flow handling
            null
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: AlarmRepository? = null

        fun getInstance(context: Context): AlarmRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AlarmRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}

object AlarmListSerializer : Serializer<AlarmListProto> {
    override val defaultValue: AlarmListProto = AlarmListProto.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): AlarmListProto {
        return AlarmListProto.parseFrom(input)
    }

    override suspend fun writeTo(t: AlarmListProto, output: OutputStream) {
        t.writeTo(output)
    }
}
