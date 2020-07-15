package drones.ui

import drones.MathUtils
import org.joml.Vector2f
import org.joml.Vector2fc

abstract class UiLayout {
    abstract val autoDimensions: Vector2fc
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
     * Called when the layout should a) compute the measurements of its children (i.e. `computedRelativePosition` and
     * `computedPosition`) and b) compute its own `autoDimensions`.
     */
    abstract fun computeChildMeasurements()
    open fun onMeasurementsComputed() {}

    fun rootComputeMeasurements(screenDimensions: Vector2fc,
                                rootPosition: Vector2fc = Vector2f(0f, 0f),
                                rootAnchor: Vector2fc = Vector2f(0f, 0f)) {
        computeChildMeasurements()
        computedDimensions.set(autoDimensions)
        computedRelativePosition.set(-rootAnchor.x(), 1f - rootAnchor.y()).mul(autoDimensions).add(rootPosition)
        onMeasurementsComputed()
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
