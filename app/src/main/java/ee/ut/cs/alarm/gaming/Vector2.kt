package ee.ut.cs.alarm.gaming

import kotlin.math.pow
import kotlin.math.sqrt

class Vec2(
    var x: Float,
    var y: Float,
) {
    operator fun plus(v: Vec2): Vec2 {
        x += v.x
        y += v.y
        return this
    }

    operator fun minus(v: Vec2): Vec2 {
        x -= v.x
        y -= v.y
        return this
    }

    operator fun times(mul: Float): Vec2 = Vec2(x * mul, y * mul)

    fun distanceTo(v: Vec2): Float = sqrt((x - v.x).toDouble().pow(2) + (y - v.y).toDouble().pow(2)).toFloat()

    fun length(): Float = sqrt(x.toDouble().pow(2) + y.toDouble().pow(2)).toFloat()
}
