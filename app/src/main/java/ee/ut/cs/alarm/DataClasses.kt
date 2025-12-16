package ee.ut.cs.alarm

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

    fun distance(other: Vec3): Float {
        val dx = x - other.x
        val dy = y - other.y
        val dz = z - other.z
        return sqrt(dx.pow(2) + dy.pow(2) + dz.pow(2))
    }

    override fun toString(): String {
        return "(${round(x * 100.0f) / 100.0f}, ${round(y * 100.0f) / 100.0f}, ${round(z * 100.0f) / 100.0f})"
    }
}