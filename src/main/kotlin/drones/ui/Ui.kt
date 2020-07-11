package drones.ui

import drones.Renderer
import org.joml.Vector2f
import org.joml.Vector2fc

abstract class Ui(private val params: Params) : Container {
    protected val dimensionsVec = Vector2f()
    protected val anchorPointVec = Vector2f()
    protected val positionVec = Vector2f()
    private val bottomLeftVec = Vector2f()
    private val bottomLeftVec2 = Vector2f()

    val containerDimensions get() = params.container.dimensions
    val containerWidth: Float get() = containerDimensions.x()
    val containerHeight: Float get() = containerDimensions.y()

    var shown: Boolean = true
    var renderer: Renderer? = null

    /** The dimensions of the UI element, in pixels */
    override val dimensions: Vector2fc get() {
        params.updateDimensions(this, dimensionsVec, params.container)
        return dimensionsVec
    }

    /** Where in this UI element is considered its center. (-1,-1) means bottom left, (1,1) means top right. */
    val anchorPoint: Vector2fc get() {
        params.updateAnchorPoint(this, anchorPointVec, params.container)
        return anchorPointVec
    }

    /** The position of the UI element. (0,0) is the bottom left corner of the screen, (screenWidth,screenHeight) is the
     * top right. */
    val position: Vector2fc get() {
        params.updatePosition(this, positionVec, params.container)
        positionVec.add(params.container.bottomLeft)
        return positionVec
    }

    override val bottomLeft: Vector2fc get() {
        bottomLeftVec2.set(anchorPoint).mul(0.5f).add(0.5f, 0.5f).mul(dimensions)
        bottomLeftVec.set(position).sub(bottomLeftVec2)
        return bottomLeftVec
    }

    enum class TextAlign(val id: Int) {
        LEFT(0),
        RIGHT(1),
        CENTER(2)
    }

    data class Params(val container: Container,
                      val updateDimensions: Ui.(dims: Vector2f, c: Container) -> Unit,
                      val updateAnchorPoint: Ui.(anchor: Vector2f, c: Container) -> Unit,
                      val updatePosition: Ui.(pos: Vector2f, c: Container) -> Unit)
}
