package drones.ui

import drones.ui.LayoutVector.Units.PERCENT
import drones.ui.LayoutVector.Units.PIXELS
import org.joml.Vector2f
import org.joml.Vector2fc

class LayoutVector(override var x: Float, override var xUnits: Units,
                   override var y: Float, override var yUnits: Units) : LayoutVectorc {
    constructor(x: Float, y: Float) : this(x, PIXELS, y, PIXELS)
    constructor(vec: Vector2fc) : this(vec.x(), vec.y())
    constructor() : this(0f, 0f)
    constructor(vec: LayoutVectorc) : this(vec.x, vec.xUnits, vec.y, vec.yUnits)

    override fun getPixelsX(parentWidth: Float): Float =
        if (xUnits == PIXELS) x
        else parentWidth * (x / 100f)

    override fun getPixelsY(parentHeight: Float): Float =
        if (yUnits == PIXELS) y
        else parentHeight * (y / 100f)

    fun toPixels(parentDimensions: Vector2fc) {
        x = getPixelsX(parentDimensions.x())
        y = getPixelsY(parentDimensions.y())
        xUnits = PIXELS
        yUnits = PIXELS
    }

    fun set(x: Float, xUnits: Units, y: Float, yUnits: Units) {
        this.x = x
        this.xUnits = xUnits
        this.y = y
        this.yUnits = yUnits
    }

    fun set(x: Float, y: Float) {
        set(x, PIXELS, y, PIXELS)
    }

    fun set(vec: Vector2fc) {
        set(vec.x(), vec.y())
    }

    fun set(vec: LayoutVectorc) {
        set(vec.x, vec.xUnits, vec.y, vec.yUnits/*, vec.minPxX, vec.minPxY*/)
    }

    fun add(x: Float, y: Float) {
        this.x += x
        this.y += y
    }

    fun sub(x: Float, y: Float) =
        add(-x, -y)

    fun maxPx(other: LayoutVectorc) {
        if (this.xUnits == PIXELS && other.xUnits == PIXELS)
            this.x = Math.max(this.x, other.x)

        if (this.yUnits == PIXELS && other.yUnits == PIXELS)
            this.y = Math.max(this.y, other.y)
    }

    fun maxPx(other: Vector2fc) {
        if (this.xUnits == PIXELS)
            this.x = Math.max(this.x, other.x())

        if (this.yUnits == PIXELS)
            this.y = Math.max(this.y, other.y())
    }

    override fun getPixelsVector(): Vector2fc =
        Vector2f(getPixelsX(0f), getPixelsY(0f))

    override fun getPixelsVector(parentDimensions: Vector2fc): Vector2fc =
        Vector2f(getPixelsX(parentDimensions.x()), getPixelsY(parentDimensions.y()))

    override fun toString(): String {
        val xu = if (xUnits == PIXELS) "px" else "%"
        val yu = if (yUnits == PIXELS) "px" else "%"
        return "($x$xu, $y$yu${/* min ${minPxX}px, ${minPxY}px)*/""}"
    }

    enum class Units {
        PIXELS,
        PERCENT
    }
}

/**
 * Represents a read-only view of a [LayoutVector].
 */
interface LayoutVectorc {
    val x: Float
    val xUnits: LayoutVector.Units
    val y: Float
    val yUnits: LayoutVector.Units

    fun getPixelsX(parentWidth: Float): Float
    fun getPixelsY(parentHeight: Float): Float

    /**
     * Returns a vector representing the dimensions in pixels if the parent was of 0 width and 0 height.
     */
    fun getPixelsVector(): Vector2fc

    /**
     * Returns a vector representing the dimensions in pixels given the parent dimensions (in pixels).
     */
    fun getPixelsVector(parentDimensions: Vector2fc): Vector2fc
}

/**
 * Sets this vector to the value of the given layout vector's pixel values when the parent dimensions are zero.
 */
fun Vector2f.set(v: LayoutVectorc): Vector2f = set(v.getPixelsX(0f), v.getPixelsY(0f))

/**
 * Sets this vector to the value of the given layout vector's pixel values using the given parent dimensions.
 */
fun Vector2f.set(v: LayoutVectorc, parentDimensions: Vector2fc): Vector2f =
    set(v.getPixelsX(parentDimensions.x()), v.getPixelsY(parentDimensions.x()))

/**
 * Sets each component in this vector to the larger of the current value of that component, and the pixel-valued
 * component of the given layout vector, if the parent dimensions are zero.
 */
fun Vector2f.max(v: LayoutVectorc): Vector2f {
    val vx = v.getPixelsX(0f)
    val vy = v.getPixelsY(0f)
    x = if (x > vx) x else vx
    y = if (y > vy) y else vy
    return this
}
