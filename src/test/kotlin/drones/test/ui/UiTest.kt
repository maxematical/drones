package drones.test.ui

import drones.ui.LayoutVector
import drones.ui.UiBoxElement
import drones.ui.UiVerticalLayout
import org.joml.Vector2f
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class UiTest {
    @Test
    fun testVerticalLayout() {
        val layout = UiVerticalLayout()
        val box1 = UiBoxElement(LayoutVector(100f, 20f))
        val box2 = UiBoxElement(LayoutVector(80f, 30f))
        layout.addChild(box1)
        layout.addChild(box2)
        layout.rootComputeMeasurements(Vector2f(0f, 50f))

        assertEquals(LayoutVector(0f, 0f), layout.autoDimensions)
        assertEquals(Vector2f(100f, 50f), layout.minDimensions)
        assertEquals(Vector2f(100f, 50f), layout.computedDimensions)
        assertEquals(Vector2f(0f, 50f), layout.computedPosition)

        assertEquals(LayoutVector(100f, 20f), box1.autoDimensions)
        assertEquals(Vector2f(0f, 0f), box1.minDimensions) // no child, no padding = no minimum
        assertEquals(Vector2f(100f, 20f), box1.computedDimensions)
        assertEquals(Vector2f(0f, 50f), box1.computedPosition)

        assertEquals(LayoutVector(80f, 30f), box2.autoDimensions)
        assertEquals(Vector2f(0f, 0f), box2.minDimensions)
        assertEquals(Vector2f(80f, 30f), box2.computedDimensions)
        assertEquals(Vector2f(0f, 30f), box2.computedPosition)
    }

    @Test
    fun testCenteredBox() {
        val outerBox = UiBoxElement(LayoutVector(100f, 100f))
        val centeredBox = UiBoxElement(LayoutVector(40f, 12f))
        outerBox.setChild(centeredBox)
        outerBox.centerChild = true
        outerBox.rootComputeMeasurements(Vector2f(300f, 300f))

        assertEquals(LayoutVector(100f, 100f), outerBox.autoDimensions)
        assertEquals(Vector2f(40f, 12f), outerBox.minDimensions) // box should be at least the size of its child
        assertEquals(Vector2f(100f, 100f), outerBox.computedDimensions)
        assertEquals(Vector2f(0f, 100f), outerBox.computedPosition)

        assertEquals(Vector2f(40f, 12f), centeredBox.computedDimensions)
        assertEquals(Vector2f(0.5f * (100f - 40f), 100f - 0.5f * (100f - 12f)), centeredBox.computedPosition)
    }

    @Test
    fun testTooltip() {
        val stuff = UiBoxElement(LayoutVector(10f, 10f))
        stuff.rootComputeMeasurements(Vector2f(1000f, 1000f), Vector2f(200f, 100f))

        assertEquals(Vector2f(200f, 110f), stuff.computedPosition)
        assertEquals(Vector2f(10f, 10f), stuff.computedDimensions)
    }

    @Test
    fun testNesting() {
        val root = UiVerticalLayout()

        val child1 = UiBoxElement(LayoutVector(40f, 12f))
        root.addChild(child1)

        val child2 = UiVerticalLayout()
        val child2A = UiBoxElement(LayoutVector(10f, 10f))
        val child2B = UiBoxElement(LayoutVector(10f, 10f))
        child2.addChild(child2A)
        child2.addChild(child2B)
        root.addChild(child2)

        val child3 = UiBoxElement(LayoutVector(40f, 40f))
        val centered = UiBoxElement(LayoutVector(2f, 2f))
        child3.setChild(centered)
        root.addChild(child3)

        root.rootComputeMeasurements(Vector2f(1000f, 1000f))

        assertEquals(Vector2f(40f, 72f), root.computedDimensions)
        assertEquals(Vector2f(0f, 72f), root.computedPosition)

        assertEquals(Vector2f(40f, 12f), child1.computedDimensions)
        assertEquals(Vector2f(0f, 72f), child1.computedPosition)

        assertEquals(Vector2f(10f, 20f), child2.computedDimensions)
        assertEquals(Vector2f(0f, 60f), child2.computedPosition)

        assertEquals(Vector2f(child2.computedPosition).add(0f, 0f), child2A.computedPosition)
        assertEquals(Vector2f(child2.computedPosition).add(0f, -10f), child2B.computedPosition)

        assertEquals(Vector2f(40f, 40f), child3.computedDimensions)
        assertEquals(Vector2f(0f, 40f), child3.computedPosition)

        assertEquals(Vector2f(2f, 2f), centered.computedDimensions)
        assertEquals(Vector2f(child3.computedPosition).add(0.5f * (40f - 2f), -0.5f * (40f - 2f)),
            centered.computedPosition)
    }
}
