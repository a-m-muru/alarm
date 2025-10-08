package ee.ut.cs.alarm.data

import android.os.Parcel
import android.os.Parcelable
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

class Alarm(time: UInt, var days: Byte, var enabled: Boolean = true)/* : Parcelable {

    companion object CREATOR: Parcelable.Creator<Alarm?> {
        override fun createFromParcel(source: Parcel?): Alarm? {
            val x  = source?.readInt()
            return Alarm(x as UInt, 0)
        }

        override fun newArray(size: Int): Array<out Alarm?>? {
            TODO("Not yet implemented")
        }
    }

    override fun describeContents(): Int {
        return Parcelable.CONTENTS_FILE_DESCRIPTOR
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(1)
    }
}*/


val al = Alarm(3600u, Day.MONDAY or Day.TUESDAY)