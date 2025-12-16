package ee.ut.cs.alarm.data

import android.os.Parcel
import android.os.Parcelable
import ee.ut.cs.alarm.data.proto.AlarmProto
import java.util.UUID

object Day {
    const val MONDAY: Byte = 0x1
    const val TUESDAY: Byte = 0x2
    const val WEDNESDAY: Byte = 0x4
    const val THURSDAY: Byte = 0x8
    const val FRIDAY: Byte = 0x10
    const val SATURDAY: Byte = 0x20
    const val SUNDAY: Byte = 0x40
}

val Days: Map<Byte, String> = mapOf(
    Day.MONDAY to "Mon",
    Day.TUESDAY to "Tue",
    Day.WEDNESDAY to "Wed",
    Day.THURSDAY to "Thu",
    Day.FRIDAY to "Fri",
    Day.SATURDAY to "Sat",
    Day.SUNDAY to "Sun")

/**
 * @param id id of the alarm.
 * @param time time of the alarm from midnight(00:00) in seconds.
 * @param days days of the week the alarm should ring. Bitmask with Monday = 1 << 0, Tuesday = 1 << 1, etc.
 * @param label label or message to be displayed with the alarm.
 * @param ringtoneUri uri of the ringtone to be played with the alarm.
 * @param enabled whether the alarm is enabled.
 */
data class Alarm(
    var id: UUID = UUID.randomUUID(),
    var time: UInt = 0u,
    var days: Byte = 0.toByte(),
    var label: String? = null,
    val ringtoneUri: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    var enabled: Boolean = true
) : Parcelable {
    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR: Parcelable.Creator<Alarm?> {
        override fun createFromParcel(source: Parcel?): Alarm {
            if (source != null) {
                val id = UUID.fromString(source.readString())
                val time = source.readInt().toUInt()
                val days = source.readByte()
                val label = source.readString()
                val ringtoneUri = source.readString()
                val createdAt = source.readLong()
                val enabled = source.readByte() == 0x01.toByte()
                return Alarm(id, time, days, label, ringtoneUri, createdAt, enabled)
            }

            return Alarm()
        }

        override fun newArray(size: Int): Array<out Alarm?> {
            return Array<Alarm?>(size){ null }
        }

        fun fromProto(proto: AlarmProto): Alarm {
            return Alarm(
                id = UUID.fromString(proto.id),
                time = proto.time.toUInt(),
                days = proto.days.toByte(),
                label = proto.label,
                ringtoneUri = proto.ringtoneUri,
                createdAt = proto.createdAt,
                enabled = proto.enabled,
            )
        }
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id.toString())
        dest.writeInt(time.toInt())
        dest.writeByte(days)
        dest.writeString(label)
        dest.writeString(ringtoneUri)
        dest.writeLong(createdAt)
        dest.writeByte(if (enabled) 0x01.toByte() else 0x00)
    }

    fun toProto(): AlarmProto {
        return AlarmProto.newBuilder()
            .setId(id.toString())
            .setTime(time.toInt())
            .setDays(days.toInt())
            .setLabel(label ?: "")
            .setRingtoneUri(ringtoneUri ?: "")
            .setCreatedAt(createdAt)
            .setEnabled(enabled)
            .build()
    }

    override fun toString(): String {
        return "Alarm $id; time $time; label $label"
    }
}