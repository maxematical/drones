package drones

import org.dyn4j.geometry.Vector2
import org.joml.Vector2f

object MathUtils {
    const val PI: Float = 3.14159265358979323846f
    const val DEG2RAD: Float = PI / 180f
    const val RAD2DEG: Float = 180f / PI

    fun sign(a: Float): Int {
        return if (a == 0f) 0
        else if (a < 0) -1
        else 1
    }
}

fun Vector2f.toDyn4j(result: Vector2 = Vector2()): Vector2 {
    result.set(this.x.toDouble(), this.y.toDouble())
    return result
}

fun Vector2.toJoml(result: Vector2f = Vector2f()): Vector2f {
    result.set(this.x, this.y)
    return result
}
