package drones.ui

import org.joml.Matrix4f
import org.joml.Vector2fc

abstract class UiElement : UiLayout(), UiBaseParams {
    abstract val provideRenderer: (UiGraphicsManager) -> UiElementRenderer
    private var renderer: UiElementRenderer? = null

    private val quadMatrix: Matrix4f = Matrix4f()
    private val invQuadMatrix: Matrix4f = Matrix4f()
    override val quadMatrixArr: FloatArray = FloatArray(16)
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

    override fun render(screenDimensions: Vector2fc, graphicsManager: UiGraphicsManager) {
        val renderer: UiElementRenderer = this.renderer ?: this.provideRenderer(graphicsManager)
        this.renderer = renderer

        this.recomputeMatrices(screenDimensions)
        renderer.render(this, screenDimensions)
        super.render(screenDimensions, graphicsManager)
    }
}
