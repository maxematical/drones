package drones

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
