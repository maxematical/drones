package drones.ui

import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector2fc

abstract class UiElement(val layout: UiLayout, val parentLayout: UiLayout) {
    abstract val autoDimensions: Vector2fc
    abstract val renderer: UiRenderer?

    val computedDimensions: Vector2f = Vector2f()
    val computedPosition: Vector2f = Vector2f()

    private val quadMatrix: Matrix4f = Matrix4f()
    private val invQuadMatrix: Matrix4f = Matrix4f()
    val quadMatrixArr: FloatArray = FloatArray(16)
    val invQuadMatrixArr: FloatArray = FloatArray(16)

    fun recomputeMatrices(screenDimensions: Vector2fc) {
        val mulX = 2f / screenDimensions.x()
        val mulY = 2f / screenDimensions.y()

        quadMatrix
            .identity()
            .translate(-1f, 1f, 0f)
            .translate(computedPosition.x() * mulX, -computedPosition.y() * mulY, 0f)
            .scale(computedDimensions.x() * mulX, computedDimensions.y() * mulY, 0f)
        quadMatrix.invert(invQuadMatrix)

        quadMatrix.get(quadMatrixArr)
        invQuadMatrix.get(invQuadMatrixArr)
    }
}
