package drones.ui

import org.joml.Vector2f
import org.joml.Vector2fc

class UiVerticalLayout : UiLayout() {
    private val mutableChildren = mutableListOf<UiLayout>()
    override val children: List<UiLayout> = mutableChildren

    private val mutableDimensions = Vector2f()
    override val autoDimensions: Vector2fc = mutableDimensions

    fun addChild(element: UiLayout) {
        mutableChildren.add(element)
    }

    override fun computeAutoMeasurements() {
        var maxWidth = 0f
        var totalHeight = 0f
        for (child in children) {
            child.computeAutoMeasurements()
            child.computedDimensions.set(child.autoDimensions)
            child.computedRelativePosition.set(0f, -totalHeight)
            child.onMeasurementsComputed()

            maxWidth = Math.max(child.computedDimensions.x(), maxWidth)
            totalHeight += child.computedDimensions.y()
        }

        mutableDimensions.set(maxWidth, totalHeight)
    }
}
