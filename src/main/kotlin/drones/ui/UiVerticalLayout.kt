package drones.ui

import org.joml.Vector2f
import org.joml.Vector2fc

class UiVerticalLayout : UiLayout() {
    private val mutableChildren = mutableListOf<UiLayout>()
    override val children: List<UiLayout> = mutableChildren

    private val mutableDimensions = Vector2f()
    override val autoDimensions: Vector2fc = mutableDimensions

    fun addChild(element: UiElement) {
        mutableChildren.add(element)
    }

    override fun computeChildMeasurements() {
        var maxWidth = 0f
        var nextY = 0f
        for (child in children) {
            child.computeChildMeasurements()
            child.computedDimensions.set(child.autoDimensions)
            child.computedPosition.set(0f, nextY)
            child.onMeasurementsComputed()

            maxWidth = Math.max(child.computedDimensions.x(), maxWidth)
            nextY += child.computedDimensions.y()
        }

        mutableDimensions.set(maxWidth, nextY)
    }
}
