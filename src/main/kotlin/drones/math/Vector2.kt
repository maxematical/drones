package drones.math

import kotlin.properties.Delegates

class Vector2(x: Float, y: Float) {
    var x: Float by Delegates.observable(x) { _, old, new -> if (new != old) cachedMagnitude = null }
    var y: Float by Delegates.observable(y) { _, old, new -> if (new != old) cachedMagnitude = null }

    val magnitude: Float
        get() {
            if (cachedMagnitude == null) {
                cachedMagnitude = Math.sqrt(sqrMagnitude.toDouble()).toFloat()
            }
            return cachedMagnitude!!
        }
    val sqrMagnitude: Float get() = (x * x) + (y * y)

    private var cachedMagnitude: Float? = null

    operator fun unaryMinus(): Vector2 {
        x = -x
        y = -y
        return this
    }

    operator fun plus(vec: Vector2): Vector2 {
        x += vec.x
        y += vec.y
        return this
    }

    operator fun minus(vec: Vector2): Vector2 {
        return this + (-vec)
    }

    operator fun times(scalar: Float): Vector2 {
        x *= scalar
        y *= scalar
        return this
    }

    operator fun times(vec: Vector2): Vector2 {
        x *= vec.x
        y *= vec.y
        return this
    }

    operator fun div(scalar: Float): Vector2 {
        return this * (1 / scalar)
    }

    operator fun div(vec: Vector2): Vector2 {
        x /= vec.x
        y /= vec.y
        return this
    }

    fun normalize(): Vector2 {
        div(magnitude)
        return this
    }
}
