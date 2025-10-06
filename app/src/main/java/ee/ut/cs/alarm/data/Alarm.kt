package ee.ut.cs.alarm.data

import ee.ut.cs.alarm.proto.AlarmProto
import ee.ut.cs.alarm.proto.AlarmListProto
import ee.ut.cs.alarm.proto.DayOfWeekProto
import java.util.*

data class Alarm(
    val id: String = UUID.randomUUID().toString(),
    val hour: Int,
    val minute: Int,
    val days: List<DayOfWeek>,
    val enabled: Boolean = true,
    val ringtoneUri: String? = null,
    val label: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toProto(): AlarmProto {
        return AlarmProto.newBuilder()
            .setId(id)
            .setHour(hour)
            .setMinute(minute)
            .addAllDays(days.map { day ->
                DayOfWeekProto.newBuilder().setValue(day.value).build()
            })
            .setEnabled(enabled)
            .setRingtoneUri(ringtoneUri ?: "")
            .setLabel(label ?: "")
            .setCreatedAt(createdAt)
            .build()
    }

    companion object {
        fun fromProto(proto: AlarmProto): Alarm {
            return Alarm(
                id = proto.id,
                hour = proto.hour,
                minute = proto.minute,
                days = proto.daysList.map { dayProto ->
                    DayOfWeek.fromValue(dayProto.value)
                },
                enabled = proto.enabled,
                ringtoneUri = if (proto.ringtoneUri.isNotEmpty()) proto.ringtoneUri else null,
                label = if (proto.label.isNotEmpty()) proto.label else null,
                createdAt = proto.createdAt
            )
        }
    }
}

enum class DayOfWeek(val value: Int, val displayName: String) {
    MONDAY(1, "M"),
    TUESDAY(2, "T"),
    WEDNESDAY(3, "W"),
    THURSDAY(4, "T"),
    FRIDAY(5, "F"),
    SATURDAY(6, "S"),
    SUNDAY(7, "S");

    companion object {
        fun fromValue(value: Int): DayOfWeek {
            return values().find { it.value == value } ?: MONDAY
        }
    }
}
