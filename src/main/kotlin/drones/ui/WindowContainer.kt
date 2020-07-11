package drones.ui

import org.joml.Vector2f
import org.joml.Vector2fc

class WindowContainer(override val dimensions: Vector2f) : Container {
    override val bottomLeft: Vector2fc = Vector2f()
}
