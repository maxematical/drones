package drones

import org.dyn4j.geometry.Vector2
import org.joml.Vector2f
import org.joml.Vector2fc

object MathUtils {
    const val PI: Float = 3.14159265358979323846f
    const val DEG2RAD: Float = PI / 180f
    const val RAD2DEG: Float = 180f / PI

    fun sign(a: Float): Int {
        return if (a == 0f) 0
        else if (a < 0) -1
        else 1
    }

    fun clamp(a: Int, b: Int, x: Int): Int = Math.min(b, Math.max(a, x))
    fun clamp(a: Float, b: Float, x: Float): Float = Math.min(b, Math.max(a, x))
    fun clamp(a: Double, b: Double, x: Double): Double = Math.min(b, Math.max(a, x))

    fun clampRotation(degrees: Float): Float {
        var result = degrees
        result %= 360
        if (result < -180) result += 360
        if (result >= 180) result -= 360
        return result
    }

    fun lerp(a: Float, b: Float, x: Float): Float {
        val x2 = MathUtils.clamp(0f, 1f, x)
        return a * (1f - x2) + b * x2
    }

    fun smoothstep(a: Float): Float =
        3f * (a * a) - 2f * (a * a * a)

    /**
     * Returns whether a number is negative. Unlike the check "a >= 0", this will return true even if a is negative
     * zero.
     */
    fun isNegative(a: Float): Boolean =
        (a.toBits() and 0x80000000.toInt()) != 0

    fun isPositive(a: Float): Boolean =
        !isNegative(a)
}

fun Vector2fc.toDyn4j(result: Vector2 = Vector2()): Vector2 {
    result.set(this.x().toDouble(), this.y().toDouble())
    return result
}

fun Vector2.toJoml(result: Vector2f = Vector2f()): Vector2f {
    result.set(this.x, this.y)
    return result
}

fun Float.f2(): String {
    return (Math.round(this * 100f) / 100f).toString()
}
