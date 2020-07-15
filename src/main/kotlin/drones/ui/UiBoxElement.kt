package drones.ui

import org.joml.Vector2f
import org.joml.Vector2fc

class UiBoxElement(private val boxMinDimensions: Vector2fc = Vector2f()) : UiElement() {
    private val mutableDimensions = Vector2f(boxMinDimensions)
    override val autoDimensions: Vector2fc = mutableDimensions

    private var child: UiLayout? = null
    private val childList = mutableListOf<UiLayout>()
    override val children: List<UiLayout> = childList

    override var renderer: UiBoxRenderer? = null

    var centerChild = true
    var borderWidth: Int = 0
    var borderColor: Int = 0xFFFFFF
    var backgroundColor: Int = 0x000067
    var padding: Float = 0f

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
            child.computedRelativePosition.set(padding, -padding)
            child.onMeasurementsComputed()
        }

        mutableDimensions.set(Math.max(child?.computedDimensions?.x() ?: 0f, boxMinDimensions.x()),
            Math.max(child?.computedDimensions?.y() ?: 0f, boxMinDimensions.y()))
        mutableDimensions.add(padding * 2f, padding * 2f)
    }

    override fun onMeasurementsComputed() {
        val child = this.child
        if (child != null && centerChild) {
            // Formula: ChildPos = (BoxDimensions - ChildDimensions) / 2
            child.computedRelativePosition.set(autoDimensions).sub(child.computedDimensions).mul(0.5f)
            child.computedRelativePosition.y *= -1
        }
    }
}
