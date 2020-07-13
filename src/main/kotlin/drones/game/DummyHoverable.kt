package drones.game

import org.joml.Vector4fc

object DummyHoverable : Hoverable {
    override fun isHover(mousePos: Vector4fc): Boolean = false
}
