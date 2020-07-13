package drones.game

import drones.MathUtils
import org.joml.Vector2f
import org.joml.Vector2fc

class LaserBeam(override val position: Vector2f,
                override var rotation: Float,
                var width: Float,
                var unobstructedLength: Float) : GameObject() {

    var actualLength: Float = unobstructedLength

    var lifetime: Float = 0f

    var colorR: Float = 1.5f
    var colorG: Float = 1.5f
    var colorB: Float = 1.5f

    override fun recomputeModelMatrix() {
        mutModelMatrix.identity()
            .translate(position.x(), position.y(), 0f)
            .rotate(rotation * MathUtils.DEG2RAD, 0f, 0f, 1f)
            .translate(actualLength * 0.5f, 0f, 0f)
            .scale(actualLength, width, 1f)
        modelMatrix.get(modelMatrixArr)
        modelMatrix.invert(mutModelMatrixInv)
    }
}
