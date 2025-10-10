package ee.ut.cs.alarm

import ee.ut.cs.alarm.gaming.Vec3
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sqrt

data class Vec3(var x: Float, var y: Float, var z: Float) {
    operator fun plus(other: Vec3): Vec3 {
        return Vec3(x + other.x, y + other.y, z + other.z)
    }
    operator fun minus(other: Vec3): Vec3 {
        return Vec3(x - other.x, y - other.y, z - other.z)
    }
    operator fun times(scalar: Float): Vec3 {
        return Vec3(x * scalar, y * scalar, z * scalar)
    }
    operator fun div(scalar: Float): Vec3 {
        return Vec3(x / scalar, y / scalar, z / scalar)
    }
    fun length(): Float {
        return sqrt(x.pow(2) + y.pow(2) + z.pow(2))
    }
    fun normalize(): Vec3 {
        val len = length()
        return Vec3(x / len, y / len, z / len)
    }
    fun toFloatArray(): FloatArray {
        return floatArrayOf(x, y, z)
    }
    override fun toString(): String {
        return "(${round(x * 100.0f) / 100.0f}, ${round(y * 100.0f) / 100.0f}, ${round(z * 100.0f) / 100.0f})"
    }
}