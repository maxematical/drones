package drones.ui

import org.joml.Vector2fc

interface UiElementRenderer {
    fun render(element: UiElement, screenDimensions: Vector2fc)
}
