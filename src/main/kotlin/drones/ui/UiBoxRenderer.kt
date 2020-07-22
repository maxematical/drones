package drones.ui

import org.joml.Vector2fc
import org.lwjgl.opengl.GL43.glGetUniformLocation
import org.lwjgl.opengl.GL43.glUniform1i

class UiBoxRenderer(graphicsManager: GraphicsManager) : UiRenderer<UiBoxParams>, UiElementRenderer {
    private val shaderProgram = graphicsManager.boxShaderProgram
    private val baseRenderer = UiBaseRenderer(shaderProgram)

    private val locationBorderWidth: Int
    private val locationBorderColor: Int
    private val locationBackgroundColor: Int

    init {
        locationBorderWidth = glGetUniformLocation(shaderProgram, "BoxBorderSize")
        locationBorderColor = glGetUniformLocation(shaderProgram, "BoxBorderColor")
        locationBackgroundColor = glGetUniformLocation(shaderProgram, "BoxBackgroundColor")
    }

    override fun render(element: UiElement, screenDimensions: Vector2fc) {
        render(screenDimensions, element as UiBoxElement)
    }

    override fun render(screenDimensions: Vector2fc, params: UiBoxParams) {
        baseRenderer.render(screenDimensions, params) {
            glUniform1i(locationBorderWidth, params.borderWidth)
            glUniform1i(locationBorderColor, params.borderColor)
            glUniform1i(locationBackgroundColor, params.backgroundColor)
        }
    }
}

interface UiBoxParams : UiBaseParams {
    val borderWidth: Int
    val borderColor: Int
    val backgroundColor: Int
    val padding: Padding
}
