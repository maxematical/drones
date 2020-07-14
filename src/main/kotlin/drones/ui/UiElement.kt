package drones.ui

import org.joml.Matrix4f
import org.joml.Vector2fc

abstract class UiElement : UiLayout() {
    abstract val renderer: UiRenderer?

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
            .translate(computedRelativePosition.x() * mulX, -computedRelativePosition.y() * mulY, 0f)
            .scale(computedDimensions.x() * mulX, computedDimensions.y() * mulY, 0f)
        quadMatrix.invert(invQuadMatrix)

        quadMatrix.get(quadMatrixArr)
        invQuadMatrix.get(invQuadMatrixArr)
    }

    override fun render(screenDimensions: Vector2fc) {
        this.renderer?.render(screenDimensions)
        super.render(screenDimensions)
    }
}
