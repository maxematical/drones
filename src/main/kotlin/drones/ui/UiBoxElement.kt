package drones.ui

import org.joml.Vector2f
import org.joml.Vector2fc

class UiBoxElement(override val autoDimensions: LayoutVectorc = LayoutVector.ZERO) : UiElement(), UiBoxParams {
    override val provideRenderer = UiGraphicsManager::boxRenderer

    private val mMinDimensions = Vector2f()
    override val minDimensions: Vector2fc = mMinDimensions

    var child: UiLayout? = null
        private set
    private val childList = mutableListOf<UiLayout>()
    override val children: List<UiLayout> = childList

    var centerChild = false
    override var borderWidth: Int = 0
    override var borderColor: Int = 0xFFFFFF
    override var backgroundColor: Int = 0x000067
    override val padding: Padding = Padding(0f)

    fun <T : UiLayout?> setChild(newChild: T): T {
        child = newChild

        childList.clear()
        if (newChild != null) {
            childList.add(newChild)
        }

        return newChild
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
        // Expand to accommodate for padding
        mMinDimensions.add(padding.totalHorizontal, padding.totalVertical)

        // Auto dimensions don't need to be computed (they are set via constructor parameter)
    }

    override fun computeFinalMeasurements() {
        val child = this.child
        if (child != null) {
            // Compute child final dimensions
            val innerDimensions = Vector2f(this.computedDimensions)
            innerDimensions.sub(padding.totalHorizontal, padding.totalVertical)

            doComputeChildDimensions(child.autoDimensions, child.minDimensions,
                innerDimensions, child.computedDimensions)

            // Compute child relative position
            if (centerChild) {
                // Centering formula: (ParentDimensions - ChildDimensions) / 2
                child.computedRelativePosition.set(this.computedDimensions).sub(child.computedDimensions).mul(0.5f)
                child.computedRelativePosition.y *= -1
            } else {
                child.computedRelativePosition.set(padding.left, -padding.top)
            }

            // Pass to child
            child.computeFinalMeasurements()
        }
    }
}
