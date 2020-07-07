package drones

import org.joml.*

class Drone(val grid: Grid,
            val position: Vector2f,
            val color: Int = 0xFFFFFF,
            val velocity: Vector2f = Vector2f(),
            var rotation: Float = 0f,
            val size: Float = 0.8f) {
    val desiredVelocity: Vector2f = Vector2f(velocity)

    var localTime: Float = 0f

    val modelMatrix: Matrix4f = Matrix4f()
    val modelMatrixArr: FloatArray = FloatArray(16)

    init {
        recomputeModelMatrix()
    }

    fun recomputeModelMatrix() {
        modelMatrix.identity()
        modelMatrix.translate(position.x, position.y, 0f)
        modelMatrix.rotate(rotation * MathUtils.DEG2RAD, 0f, 0f, 1f)
        modelMatrix.scale(size)
        modelMatrix.get(modelMatrixArr)
    }
}
