package drones.ui

import org.joml.Vector2f
import org.joml.Vector2fc

abstract class UiLayout {
    abstract val autoDimensions: Vector2fc
    abstract val children: List<UiLayout>

    val computedDimensions: Vector2f = Vector2f()

    /**
     * The computed bottom-left position, in pixels, of this layout element.
     */
    val computedPosition: Vector2f = Vector2f()

    abstract fun computeChildMeasurements()
    open fun onMeasurementsComputed() {}

    fun rootComputeMeasurements(rootBottomLeft: Vector2fc = Vector2f(0f, 0f),
                                rootAnchor: Vector2fc = Vector2f(-1f, -1f)) {
        computeChildMeasurements()
        computedDimensions.set(autoDimensions)
        // Formula: computedPosition = rootBottomLeft + (rootAnchor * 0.5 + 0.5) * autoDimensions
        computedPosition.set(rootAnchor).mul(0.5f).add(0.5f, 0.5f).mul(autoDimensions).add(rootBottomLeft)
        onMeasurementsComputed()
    }

    open fun render(screenDimensions: Vector2fc) {
        for (child in children) {
            child.render(screenDimensions)
        }
    }
}
