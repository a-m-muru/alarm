package ee.ut.cs.alarm.data.repo

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import ee.ut.cs.alarm.data.Alarm
import ee.ut.cs.alarm.data.proto.AlarmListProto
import ee.ut.cs.alarm.data.proto.GlobalMeta
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

object AlarmListSerializer : Serializer<AlarmListProto> {
    override val defaultValue: AlarmListProto = AlarmListProto.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): AlarmListProto = AlarmListProto.parseFrom(input)

    override suspend fun writeTo(
        t: AlarmListProto,
        output: OutputStream,
    ) {
        t.writeTo(output)
    }
}

interface AlarmRepository {
    suspend fun getAlarms(): Flow<List<Alarm>>

    suspend fun saveAlarm(alarm: Alarm)

    suspend fun deleteAlarm(alarm: Alarm)

    suspend fun getAlarmByID(alarmID: UUID): Alarm?

    suspend fun getStreak(): Int

    suspend fun setStreak(to: Int)

    suspend fun getPreviousStreak(): Int

    suspend fun setPreviousStreak(to: Int)
}

class AlarmRepositoryImpl private constructor(
    private val context: Context,
) : AlarmRepository {
    private val Context.alarmDataStore: DataStore<AlarmListProto> by dataStore(
        fileName = "alarms.pb",
        serializer = AlarmListSerializer,
    )

    override suspend fun getAlarms(): Flow<List<Alarm>> =
        context.alarmDataStore.data
            .catch { exception -> throw exception }
            .map { alarmList ->
                alarmList.alarmsList.map { Alarm.fromProto(it) }
            }

    /**
     * @param alarm alarm to save.
     */
    override suspend fun saveAlarm(alarm: Alarm) {
        context.alarmDataStore.updateData { currentList ->
            val builder = currentList.toBuilder()
            val existingIndex = currentList.alarmsList.indexOfFirst { it.id == alarm.id.toString() }

            if (existingIndex >= 0) {
                builder.setAlarms(existingIndex, alarm.toProto())
            } else {
                builder.addAlarms(alarm.toProto())
            }

            builder.build()
        }
    }

    /**
     * @param alarm alarm to delete.
     */
    override suspend fun deleteAlarm(alarm: Alarm) {
        context.alarmDataStore.updateData { currentList ->
            val existingIndex = currentList.alarmsList.indexOfFirst { it.id == alarm.id.toString() }
            if (existingIndex >= 0) {
                currentList
                    .toBuilder()
                    .removeAlarms(existingIndex)
                    .build()
            } else {
                currentList
            }
        }
    }

    override suspend fun getAlarmByID(alarmID: UUID): Alarm? =
        context.alarmDataStore.data
            .map { alarmList ->
                alarmList.alarmsList
                    .find { it.id == alarmID.toString() }
                    ?.let { Alarm.fromProto(it) }
            }.catch { null }
            .let {
                null
            }

    // #region CHATGPT
    override suspend fun getStreak(): Int =
        context.alarmDataStore.data
            .map { it.meta?.streak ?: 0 }
            .first()
            .toInt()

    override suspend fun getPreviousStreak(): Int =
        context.alarmDataStore.data
            .map { it.meta?.previousStreak ?: 0 }
            .first()
            .toInt()

    override suspend fun setStreak(to: Int) {
        context.alarmDataStore.updateData { current ->
            val meta = current.meta?.toBuilder() ?: GlobalMeta.newBuilder()
            meta.streak = to
            current.toBuilder().setMeta(meta).build()
        }
    }

    override suspend fun setPreviousStreak(to: Int) {
        context.alarmDataStore.updateData { current ->
            val meta = current.meta?.toBuilder() ?: GlobalMeta.newBuilder()
            meta.previousStreak = to
            current.toBuilder().setMeta(meta).build()
        }
    }
    // #endregion CHATGPT

    companion object {
        @Volatile
        private var singletonInstance: AlarmRepository? = null

        fun getInstance(context: Context): AlarmRepository =
            singletonInstance ?: synchronized(this) {
                singletonInstance ?: AlarmRepositoryImpl(context.applicationContext).also {
                    singletonInstance = it
                }
            }
    }
}
