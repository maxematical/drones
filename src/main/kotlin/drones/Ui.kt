package drones

import org.joml.Vector2f
import org.joml.Vector2fc

abstract class Ui(private val params: Params) {
    protected val dimensionsVec = Vector2f()
    protected val anchorPointVec = Vector2f()
    protected val positionVec = Vector2f()

    val screenDimensions get() = params.screenDimensions
    val screenWidth: Float get() = screenDimensions.x()
    val screenHeight: Float get() = screenDimensions.y()

    var renderer: Renderer? = null

    /** The dimensions of the UI element, in pixels */
    val dimensions: Vector2fc get() {
        params.updateDimensions(this, dimensionsVec)
        return dimensionsVec
    }

    /** Where in this UI element is considered its center. (-1,-1) means bottom left, (1,1) means top right. */
    val anchorPoint: Vector2fc get() {
        params.updateAnchorPoint(this, anchorPointVec)
        return anchorPointVec
    }

    /** The position of the UI element. (0,0) is the bottom left corner of the screen, (screenWidth,screenHeight) is the
     * top right. */
    val position: Vector2fc get() {
        params.updatePosition(this, positionVec)
        return positionVec
    }

    enum class TextAlign(val id: Int) {
        LEFT(0),
        RIGHT(1),
        CENTER(2)
    }

    data class Params(val screenDimensions: Vector2fc,
                      val updateDimensions: Ui.(dims: Vector2f) -> Unit,
                      val updateAnchorPoint: Ui.(anchor: Vector2f) -> Unit,
                      val updatePosition: Ui.(pos: Vector2f) -> Unit)
}
