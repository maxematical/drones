package drones.ui

import org.lwjgl.opengl.GL43.glGetUniformLocation
import org.lwjgl.opengl.GL43.glUniform1i

class UiBoxRenderer(private val element: UiBoxElement, shaderProgram: Int) : UiRenderer(element, shaderProgram) {
    private val locationBorderWidth: Int
    private val locationBorderColor: Int
    private val locationBackgroundColor: Int

    init {
        locationBorderWidth = glGetUniformLocation(shaderProgram, "BoxBorderSize")
        locationBorderColor = glGetUniformLocation(shaderProgram, "BoxBorderColor")
        locationBackgroundColor = glGetUniformLocation(shaderProgram, "BoxBackgroundColor")
    }

    override fun setUniforms() {
        glUniform1i(locationBorderWidth, element.borderWidth)
        glUniform1i(locationBorderColor, element.borderColor)
        glUniform1i(locationBackgroundColor, element.backgroundColor)
    }
}
