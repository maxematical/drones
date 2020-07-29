package drones.render

import org.joml.Vector2fc

object DummyRenderer : Renderer {
    override fun render(screenDimensions: Vector2fc, cameraMatrixArr: FloatArray, time: Float, drawTime: Float) {}
}
