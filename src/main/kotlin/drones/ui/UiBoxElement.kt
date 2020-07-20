package drones.ui

import org.joml.Vector2f
import org.joml.Vector2fc

class UiBoxElement(private val baseDimensions: LayoutVector = LayoutVector()) : UiElement() {
    private val mAutoDimensions = LayoutVector()
    override val autoDimensions: LayoutVector = mAutoDimensions

    private val mMinDimensions = Vector2f()
    override val minDimensions: Vector2fc = mMinDimensions

    private var child: UiLayout? = null
    private val childList = mutableListOf<UiLayout>()
    override val children: List<UiLayout> = childList

    override var renderer: UiBoxRenderer? = null

    var centerChild = true
    var borderWidth: Int = 0
    var borderColor: Int = 0xFFFFFF
    var backgroundColor: Int = 0x000067
    val padding: Padding = Padding(0f)

    fun setChild(newChild: UiLayout?) {
        child = newChild

        childList.clear()
        if (newChild != null) {
            childList.add(newChild)
        }
    }

    override fun computeAutoMeasurements() {
        val child = this.child
        child?.computeAutoMeasurements()

        // Compute min dimensions
        mMinDimensions.set(0f)
        if (child != null) {
            // Expand to accommodate for child
            mMinDimensions.set(child.autoDimensions)
            mMinDimensions.max(child.minDimensions)
        }

        // Determine our dimensions
        mAutoDimensions.set(baseDimensions)
    }

    override fun computeFinalMeasurements() {
        val child = this.child
        if (child != null) {
            doComputeChildDimensions(child)
        }
    }

    override fun onMeasurementsComputed() {
        val child = this.child

        if (child != null) {
            // Compute final child width/height
            child.computedDimensions.set(Vector2f(computedDimensions).sub(padding.totalHorizontal, padding.totalVertical))
            child.computedRelativePosition.set(padding.left, -padding.top)
            child.onMeasurementsComputed()
        }



        if (child != null && centerChild) {
            // Formula: ChildPos = (BoxDimensions - ChildDimensions) / 2
            child.computedRelativePosition.set(computedDimensions).sub(child.computedDimensions).mul(0.5f)
            child.computedRelativePosition.y *= -1
        }

        if (child != null) {
            child.computedDimensions
        }
    }
}
