package ee.ut.cs.alarm.data.repo

import android.content.Context
import android.util.Log
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

    fun streakFlow(): Flow<Int> // gpt

    fun previousStreakFlow(): Flow<Int> // gpt

    suspend fun getStreak(): Int // gpt

    suspend fun setStreak(to: Int) // gpt

    suspend fun getPreviousStreak(): Int // gpt

    suspend fun setPreviousStreak(to: Int) // gpt

    suspend fun getLastStreakDay(): Int // gpt

    suspend fun setLastStreakDay(dayInt: Int) // gpt

    fun lastStreakDayFlow(): kotlinx.coroutines.flow.Flow<Int> // gpt
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
    override fun streakFlow(): Flow<Int> = context.alarmDataStore.data.map { it.meta?.streak ?: 0 }

    override fun previousStreakFlow(): Flow<Int> = context.alarmDataStore.data.map { it.meta?.previousStreak ?: 0 }

    override suspend fun getStreak(): Int {
        val value =
            context.alarmDataStore.data
                .map { it.meta?.streak ?: 0 }
                .first()
        Log.d("ALARM REPOSITORY", "value of streak is $value")
        return value
    }

    override suspend fun getPreviousStreak(): Int {
        val value =
            context.alarmDataStore.data
                .map { it.meta?.previousStreak ?: 0 }
                .first()
        Log.d("ALARM REPOSITORY", "value of previous streak is $value")
        return value
    }

    override suspend fun setStreak(to: Int) {
        Log.d("ALARM REPOSITORY", "setting streak to $to")
        context.alarmDataStore.updateData { current ->
            val meta = current.meta?.toBuilder() ?: GlobalMeta.newBuilder()
            meta.streak = to
            current.toBuilder().setMeta(meta).build()
        }
        // getStreak()
    }

    override suspend fun setPreviousStreak(to: Int) {
        Log.d("ALARM REPOSITORY", "setting previous streak to $to")
        context.alarmDataStore.updateData { current ->
            val meta = current.meta?.toBuilder() ?: GlobalMeta.newBuilder()
            meta.previousStreak = to
            current.toBuilder().setMeta(meta).build()
        }
        // getPreviousStreak()
    }

    override suspend fun getLastStreakDay(): Int =
        context.alarmDataStore.data
            .map { it.meta?.lastStreakDay ?: 0 }
            .first()

    override suspend fun setLastStreakDay(dayInt: Int) {
        context.alarmDataStore.updateData { current ->
            val metaBuilder = current.meta?.toBuilder() ?: GlobalMeta.newBuilder()
            metaBuilder.lastStreakDay = dayInt
            current.toBuilder().setMeta(metaBuilder).build()
        }
    }

    override fun lastStreakDayFlow(): Flow<Int> = context.alarmDataStore.data.map { it.meta?.lastStreakDay ?: 0 }

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
