package drones.ui

import org.joml.Vector2f
import org.joml.Vector2fc

class UiBoxElement(private val boxMinDimensions: Vector2fc) : UiElement() {
    private val mutableDimensions = Vector2f(boxMinDimensions)
    override val autoDimensions: Vector2fc = mutableDimensions

    override val renderer: UiRenderer? = null

    private var child: UiLayout? = null
    private val childList = mutableListOf<UiLayout>()
    override val children: List<UiLayout> = childList

    var shouldCenterChild = true

    fun setChild(newChild: UiLayout?) {
        child = newChild

        childList.clear()
        if (newChild != null) {
            childList.add(newChild)
        }
    }

    override fun computeChildMeasurements() {
        val child = this.child
        if (child != null) {
            child.computeChildMeasurements()
            child.computedDimensions.set(child.autoDimensions)
            child.computedRelativePosition.set(0f, 0f)
            child.onMeasurementsComputed()
        }

        mutableDimensions.set(Math.max(child?.computedDimensions?.x() ?: 0f, boxMinDimensions.x()),
            Math.max(child?.computedDimensions?.y() ?: 0f, boxMinDimensions.y()))
    }

    override fun onMeasurementsComputed() {
        val child = this.child
        if (child != null) {
            // Formula: ChildPos = (BoxDimensions - ChildDimensions) / 2
            child.computedRelativePosition.set(autoDimensions).sub(child.computedDimensions).mul(0.5f)
        }
    }
}
