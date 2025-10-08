package ee.ut.cs.alarm.data

import kotlin.experimental.or

object Day {
    const val MONDAY: Byte = 0x1
    const val TUESDAY: Byte = 0x2
    const val WEDNESDAY: Byte = 0x4
    const val THURSDAY: Byte = 0x8
    const val FRIDAY: Byte = 0x10
    const val SATURDAY: Byte = 0x20
    const val SUNDAY: Byte = 0x40
}

class Alarm(time: UInt, var days: Byte, var enabled: Boolean = true)


val al = Alarm(3600u, Day.MONDAY or Day.TUESDAY)