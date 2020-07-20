package drones.ui

import drones.MathUtils
import org.joml.Vector2f
import org.joml.Vector2fc

abstract class UiLayout {
    abstract val autoDimensions: LayoutVectorc
    abstract val minDimensions: Vector2fc
    abstract val children: List<UiLayout>

    val computedDimensions: Vector2f = Vector2f()

    /**
     * The computed top-left position of this layout element. This is always y-up. If the y-coordinate is positive, it
     * is relative to the bottom left corner of the parent. If the y-coordinate is negative, it is relative to the top
     * left corner of the parent.
     */
    val computedRelativePosition: Vector2f = Vector2f()

    /**
     * The computed absolute (relative to window) top-left position of this layout element. (y-up)
     */
    val computedPosition: Vector2f = Vector2f()

    /**
     * Called when the layout should a) FIRST call computeAutoMeasurements on each of its children, b) compute its own
     * autoDimensions and minDimensions. (Children should be called first, because minDimensions often depends on what
     * children dimensions are)
     */
    abstract fun computeAutoMeasurements()

    /**
     * Called when the layout should a) compute the [computedDimensions] and [computedRelativePosition] for each child,
     * then b) call this function on each child.
     *
     * (This layout's dimensions and relative position have already been computed.)
     *
     * You do not need to compute the absolute [computedPosition], this will be done automatically.
     *
     * It is recommended to use [doComputeChildDimensions], then set the child's relative position manually.
     */
    abstract fun computeFinalMeasurements()

    /**
     * Helper function. This is the recommended way to calculate child dimensions while respecting minimum-dimensions
     * and relative-dimensions.
     */
    protected fun doComputeChildDimensions(childAutoDimensions: LayoutVectorc, childMinDimensions: Vector2fc,
                                           parentDimensions: Vector2fc,
                                           out: Vector2f) {
        out.set(childAutoDimensions, parentDimensions)
        out.max(childMinDimensions)
    }

    /**
     * @see doComputeChildDimensions
     */
    protected fun doComputeChildDimensions(child: UiLayout) {
        doComputeChildDimensions(child.autoDimensions, child.minDimensions,
            this.computedDimensions, child.computedDimensions)
    }

    fun rootComputeMeasurements(screenDimensions: Vector2fc,
                                rootPosition: Vector2fc = Vector2f(0f, 0f),
                                rootAnchor: Vector2fc = Vector2f(0f, 0f)) {
        computeAutoMeasurements()
//        computedDimensions.set(autoDimensions.getPixelsX(screenDimensions.x()),
//            autoDimensions.getPixelsY(screenDimensions.y()))
//        computedRelativePosition.set(-rootAnchor.x(), 1f - rootAnchor.y()).mul(computedDimensions).add(rootPosition)

        doComputeChildDimensions(this.autoDimensions, this.minDimensions, screenDimensions, computedDimensions)
        this.computedRelativePosition.set(-rootAnchor.x(), 1f - rootAnchor.y())
            .mul(computedDimensions).add(rootPosition)

        computeFinalMeasurements()
        computeAbsolutePosition(Vector2f(0f, screenDimensions.y()), screenDimensions)
    }

    fun rootUpdatePosition(screenDimensions: Vector2fc,
                           newRootPosition: Vector2fc,
                           rootAnchor: Vector2fc = Vector2f(0f, 0f)) {
        // No need to compute auto measurements or change any dimensions here -- we are simply translating the UI around
        // We also don't need to call computeFinalMeasurements again, relative positions should stay the same, only the
        // root layout is moving around
        computedRelativePosition.set(-rootAnchor.x(), 1f - rootAnchor.y()).mul(computedDimensions).add(newRootPosition)
        computeAbsolutePosition(Vector2f(0f, screenDimensions.y()), screenDimensions)
    }

    /**
     * The final part of the measurement computing process. The absolute position [computedPosition] is set based on
     * the given parent's position and this element's [computedRelativePosition].
     *
     * @param parentPosition the parent's absolute, top-left position
     */
    private fun computeAbsolutePosition(parentPosition: Vector2fc, parentDimensions: Vector2fc) {
        computedPosition.x = computedRelativePosition.x() + parentPosition.x()
        if (MathUtils.isPositive(computedRelativePosition.y())) {
            computedPosition.y = computedRelativePosition.y() + parentPosition.y() - parentDimensions.y()
        } else {
            computedPosition.y = computedRelativePosition.y() + parentPosition.y()
        }

        for (child in children) {
            child.computeAbsolutePosition(computedPosition, computedDimensions)
        }
    }

    open fun render(screenDimensions: Vector2fc) {
        for (child in children) {
            child.render(screenDimensions)
        }
    }
}
