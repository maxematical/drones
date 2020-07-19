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
        quadMatrix
            .identity()
            .translate(-1f, -1f, 0f)
            .scale(2f / screenDimensions.x(), 2f / screenDimensions.y(), 0f)
            .translate(computedPosition.x(), computedPosition.y(), 0f)
            .scale(computedDimensions.x(), computedDimensions.y(), 0f)
        quadMatrix.invert(invQuadMatrix)

        quadMatrix.get(quadMatrixArr)
        invQuadMatrix.get(invQuadMatrixArr)
    }

    override fun render(screenDimensions: Vector2fc) {
        this.renderer?.render(screenDimensions)
        super.render(screenDimensions)
    }
}
