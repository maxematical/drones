package drones.ui

import org.joml.Vector2f
import org.joml.Vector2fc

class UiVerticalLayout(override val autoDimensions: LayoutVectorc = LayoutVector.ZERO) : UiLayout() {
    private val mutableChildren = mutableListOf<UiLayout>()
    override val children: List<UiLayout> = mutableChildren

    private val mMinDimensions = Vector2f()
    override val minDimensions: Vector2fc = mMinDimensions

    fun <T : UiLayout> addChild(element: T): T {
        mutableChildren.add(element)
        return element
    }

    fun clearChildren() {
        mutableChildren.clear()
    }

    override fun computeAutoMeasurements() {
        var maxWidth = 0f
        var totalHeight = 0f
        for (child in children) {
            child.computeAutoMeasurements()

            val childWidth = Math.max(child.autoDimensions.getPixelsX(0f), child.minDimensions.x())
            val childHeight = Math.max(child.autoDimensions.getPixelsY(0f), child.minDimensions.y())
            maxWidth = Math.max(childWidth, maxWidth)
            totalHeight += childHeight
        }

        mMinDimensions.set(maxWidth, totalHeight)
    }

    override fun computeFinalMeasurements() {
        val parentDimensions = Vector2f(this.computedDimensions.x, 0f)
        var y = 0f

        for (child in children) {
            // Compute the child's dimensions
            // Note that here, we are using "parentDimensions" instead of this.computedDimensions -- this vector has a
            // y-component of zero, meaning that %-height children will end up just getting 0px of height instead.
            // It wouldn't make sense for a child to e.g. 100% height child to take up all the space in the vertical
            // layout.
            doComputeChildDimensions(child.autoDimensions, child.minDimensions, parentDimensions,
                child.computedDimensions)

            // Compute the child's relative position
            child.computedRelativePosition.set(0f, -y)
            y += child.computedDimensions.y
        }

        for (child in children)
            child.computeFinalMeasurements()
    }
}
