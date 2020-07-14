package drones.ui

import org.joml.Vector2f
import org.joml.Vector2fc

abstract class UiLayout {
    abstract val autoDimensions: Vector2fc
    abstract val children: List<UiLayout>

    val computedDimensions: Vector2f = Vector2f()

    /**
     * The computed, relative-to-parent, top-left position of this layout element. (Pixels)
     */
    val computedRelativePosition: Vector2f = Vector2f()

    /**
     * The computed absolute (relative to window) top-left position of this layout element.
     */
    val computedPosition: Vector2f = Vector2f()

    abstract fun computeChildMeasurements()
    open fun onMeasurementsComputed() {}

    fun rootComputeMeasurements(rootPosition: Vector2fc = Vector2f(0f, 0f),
                                rootAnchor: Vector2fc = Vector2f(0f, 0f)) {
        computeChildMeasurements()
        computedDimensions.set(autoDimensions)
        computedRelativePosition.set(rootAnchor).mul(autoDimensions).add(rootPosition)
        onMeasurementsComputed()
        computeAbsolutePosition(Vector2f(0f, 0f))
    }

    fun computeAbsolutePosition(parentAbsolutePosition: Vector2fc) {
        computedPosition.set(computedRelativePosition).add(parentAbsolutePosition)
        for (child in children) {
            child.computeAbsolutePosition(this.computedPosition)
        }
    }

    open fun render(screenDimensions: Vector2fc) {
        for (child in children) {
            child.render(screenDimensions)
        }
    }
}
