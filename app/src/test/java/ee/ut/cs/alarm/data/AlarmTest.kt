package ee.ut.cs.alarm.data

import android.os.Parcel
import ee.ut.cs.alarm.data.proto.AlarmProto
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.UUID
import kotlin.experimental.or


@RunWith(RobolectricTestRunner::class)
class AlarmTest {

    @Test
    fun testAlarmCreation() {
        val id = UUID.randomUUID()
        val time = (30600).toUInt()
        val days = Day.MONDAY
        val label = "Test Alarm"
        val ringtoneUri = "test_uri"
        val createdAt = System.currentTimeMillis()
        val enabled = true

        val alarm = Alarm(
            id = id,
            time = time,
            days = days,
            label = label,
            ringtoneUri = ringtoneUri,
            createdAt = createdAt,
            enabled = enabled
        )

        assertEquals(id, alarm.id)
        assertEquals(time, alarm.time)
        assertEquals(days, alarm.days)
        assertEquals(label, alarm.label)
        assertEquals(ringtoneUri, alarm.ringtoneUri)
        assertEquals(createdAt, alarm.createdAt)
        assertEquals(enabled, alarm.enabled)
    }

    @Test
    fun testParcelableImplementation() {
        val originalAlarm = Alarm(
            id = UUID.randomUUID(),
            time = 36000u,
            days = Day.SATURDAY or Day.SUNDAY,
            label = "Test Alarm",
            ringtoneUri = "test_uri",
            createdAt = System.currentTimeMillis(),
            enabled = true
        )

        val parcel = Parcel.obtain()
        originalAlarm.writeToParcel(parcel, originalAlarm.describeContents())
        parcel.setDataPosition(0)
        val createdFromParcel = Alarm.CREATOR.createFromParcel(parcel)
        parcel.recycle()
        
        assertEquals(originalAlarm, createdFromParcel)
    }


    @Test
    fun testDefaultValues() {
        val defaultAlarm = Alarm()

        // assertEquals(expected, actual)
        assertEquals(0u, defaultAlarm.time)
        assertEquals(0.toByte(), defaultAlarm.days)
        assertEquals(null, defaultAlarm.label)
        assertEquals(null, defaultAlarm.ringtoneUri)
        assertEquals(true, defaultAlarm.enabled)
        //check for alarm time to be recent
        val timeDifference = System.currentTimeMillis() - defaultAlarm.createdAt
        assert(timeDifference < 1000)
    }

    @Test
    fun testProtoRoundTrip() {
        val originalAlarm = Alarm(
            id = UUID.randomUUID(),
            time = 36000u,
            days = Day.TUESDAY or Day.THURSDAY or Day.FRIDAY,
            label = "Test Alarm",
            ringtoneUri = "test_uri",
            createdAt = System.currentTimeMillis(),
            enabled = true
        )

        val proto = originalAlarm.toProto()
        val restoredAlarm = Alarm.fromProto(proto)

        assertEquals(originalAlarm, restoredAlarm)
    }
}
