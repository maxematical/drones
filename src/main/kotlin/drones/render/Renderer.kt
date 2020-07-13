package drones.render

import org.joml.Vector2fc

interface Renderer {
    fun render(screenDimensions: Vector2fc, cameraMatrixArr: FloatArray, time: Float)
}
