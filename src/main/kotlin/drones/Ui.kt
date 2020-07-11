package drones

import org.joml.Vector2f
import org.joml.Vector2fc

abstract class Ui(val screenDimensions: Vector2f) {
    protected val dimensionsVec = Vector2f()
    protected val positionVec = Vector2f()
    protected val anchorPointVec = Vector2f()

    val screenWidth: Float get() = screenDimensions.x
    val screenHeight: Float get() = screenDimensions.y

    var renderer: Renderer? = null
    var requestedString: String? = null
    var textBgColor: Int = 0
    var textFgColor: Int = 15
    var transparentTextBg: Boolean = false
    var textAlign: TextAlign = TextAlign.LEFT

    /** The dimensions of the UI element, in pixels */
    val dimensions: Vector2fc get() {
        updateDimensions()
        return dimensionsVec
    }

    /** The position of the UI element. (0,0) is the bottom left corner of the screen, (screenWidth,screenHeight) is the
     * top right. */
    val position: Vector2fc get() {
        updatePosition()
        return positionVec
    }

    /** Where in this UI element is considered its center. (-1,-1) means bottom left, (1,1) means top right. */
    val anchorPoint: Vector2fc get() {
        updateAnchorPoint()
        return anchorPointVec
    }

    protected abstract fun updateDimensions()
    protected abstract fun updatePosition()
    protected abstract fun updateAnchorPoint()

    enum class TextAlign(val id: Int) {
        LEFT(0),
        RIGHT(1),
        CENTER(2)
    }
}
