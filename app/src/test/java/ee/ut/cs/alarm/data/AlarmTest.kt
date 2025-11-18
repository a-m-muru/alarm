package ee.ut.cs.alarm.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.UUID

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
}
