package drones

import org.joml.Vector4f
import org.joml.Vector4fc

class DroneHoverable(private val drone: Drone) : Hoverable {
    override fun isHover(mousePos: Vector4fc): Boolean {
        val transformedMousePos = Vector4f(mousePos)
        drone.modelMatrixInv.transform(transformedMousePos)

        return Math.abs(transformedMousePos.x) <= 0.5 && Math.abs(transformedMousePos.y) <= 0.5
    }
}
