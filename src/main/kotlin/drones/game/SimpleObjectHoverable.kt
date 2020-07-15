package drones.game

import org.joml.Vector4f
import org.joml.Vector4fc

class SimpleObjectHoverable(private val obj: GameObject) : Hoverable {
    override fun isHover(mousePos: Vector4fc): Boolean {
        val transformedMousePos = Vector4f(mousePos)
        obj.modelMatrixInv.transform(transformedMousePos)

        return Math.abs(transformedMousePos.x) <= 0.5 && Math.abs(transformedMousePos.y) <= 0.5
    }
}
