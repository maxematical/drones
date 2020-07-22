package drones.ui

import org.joml.Vector2fc
import org.lwjgl.opengl.GL43.*

interface UiRenderer<T : UiBaseParams> {
    fun render(screenDimensions: Vector2fc, params: T)
}
