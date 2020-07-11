package drones.ui

import drones.Renderer
import org.joml.Vector2f
import org.joml.Vector2fc

abstract class Ui(private val params: Params) : Container {
    protected val dimensionsVec = Vector2f()
    private val dimensionsWithoutPaddingVec = Vector2f()
    protected val anchorPointVec = Vector2f()
    protected val positionVec = Vector2f()
    protected val paddingObj = Padding()
    private val topLeftVec = Vector2f()
    private val bottomLeftVec = Vector2f()

    val containerDimensions get() = params.container.dimensions
    val containerWidth: Float get() = containerDimensions.x()
    val containerHeight: Float get() = containerDimensions.y()

    var shown: Boolean = true
    var renderer: Renderer? = null

    private val mutChildren = mutableListOf<Ui>()
    /** The immediate children of this UI element. (Not recursive) */
    val children: List<Ui> = mutChildren

    init {
        if (params.container is Ui) {
            params.container.mutChildren.add(this)
        }
    }

    /** The dimensions of the UI element, in pixels */
    override val dimensions: Vector2fc get() {
        params.updateDimensions(this, dimensionsVec, params.container)

        // Expand this UI element to fit children, if necessary
        if (params.allowOverflowX || params.allowOverflowY) {
            var childrenWidth: Float = 0f
            var childrenHeight: Float = 0f
            for (child in children) {
                val childDimensions = child.dimensions
                childrenWidth += childDimensions.x()
                childrenHeight += childDimensions.y()
            }

            if (params.allowOverflowX && dimensionsVec.x < childrenWidth)
                dimensionsVec.x = childrenWidth
            if (params.allowOverflowY && dimensionsVec.y < childrenHeight)
                dimensionsVec.y = childrenHeight
        }

        dimensionsWithoutPaddingVec.set(dimensionsVec)
        dimensionsVec.add(padding.left + padding.right, padding.top + padding.bottom)
        return dimensionsVec
    }

    /** Where in this UI element is considered its center. (-1,-1) means bottom left, (1,1) means top right. */
    val anchorPoint: Vector2fc get() {
        params.updateAnchorPoint(this, anchorPointVec, params.container)
        return anchorPointVec
    }

    /** The position of the UI element's anchor point. (0,0) is the bottom left corner of the screen,
     * (screenWidth,screenHeight) is the top right. */
    val position: Vector2fc get() {
        params.updatePosition(this, positionVec, params.container)
        positionVec.add(params.container.bottomLeft)
        return positionVec
    }

    /** How much children in this UI element will be indented, in pixels. Also affects width. Similar to CSS padding. */
    val padding: Padding get() {
        params.updatePadding(this, paddingObj, params.container)
        return paddingObj
    }

    override val bottomLeft: Vector2fc get() {
        // The formula is:
        // bottomLeft = position - (anchorPoint * 0.5 + 0.5) * dimensions + vec2(paddingLeft, paddingBottom)

        dimensions // update dimensions vectors

        bottomLeftVec.set(anchorPoint).mul(0.5f).add(0.5f, 0.5f).mul(dimensions)
        bottomLeftVec.mul(-1f).add(position)
        bottomLeftVec.add(padding.left, padding.bottom)
        return bottomLeftVec
    }

    fun removeChild(child: Ui) {
        mutChildren.remove(child)
    }

    enum class TextAlign(val id: Int) {
        LEFT(0),
        RIGHT(1),
        CENTER(2)
    }

    data class Params(val container: Container,
                      val updateDimensions: Ui.(dims: Vector2f, c: Container) -> Unit,
                      val updateAnchorPoint: Ui.(anchor: Vector2f, c: Container) -> Unit,
                      val updatePosition: Ui.(pos: Vector2f, c: Container) -> Unit,
                      val updatePadding: Ui.(pad: Padding, c: Container) -> Unit,
                      val allowOverflowX: Boolean,
                      val allowOverflowY: Boolean) {
        constructor(container: Container,
                    updateDimensions: Ui.(dims: Vector2f, c: Container) -> Unit,
                    updateAnchorPoint: Ui.(anchor: Vector2f, c: Container) -> Unit,
                    updatePosition: Ui.(pos: Vector2f, c: Container) -> Unit,
                    updatePadding: Ui.(pad: Padding, c: Container) -> Unit = { pad, _ -> pad.set(0f) },
                    allowOverflow: Boolean = true) :
                this(container, updateDimensions, updateAnchorPoint, updatePosition,
                    updatePadding, allowOverflow, allowOverflow)
    }

    class Padding(var top: Float = 0f, var left: Float = 0f, var bottom: Float = 0f, var right: Float = 0f) {
        fun set(amount: Float) {
            set(amount, amount)
        }

        fun set(vertical: Float, horizontal: Float) {
            set(vertical, horizontal, vertical, horizontal)
        }

        fun set(top: Float, left: Float, bottom: Float, right: Float) {
            this.top = top
            this.left = left
            this.bottom = bottom
            this.right = right
        }
    }
}
