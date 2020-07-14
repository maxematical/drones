package drones.ui

import org.lwjgl.opengl.GL43.glGetUniformLocation
import org.lwjgl.opengl.GL43.glUniform1i

class UiBoxRenderer(private val element: UiBoxElement, shaderProgram: Int) : UiRenderer(element, shaderProgram) {
    private val locationBorderWidth: Int
    private val locationBorderColor: Int

    init {
        locationBorderWidth = glGetUniformLocation(shaderProgram, "BoxBorderWidth")
        locationBorderColor = glGetUniformLocation(shaderProgram, "BoxBorderColor")
    }

    override fun setUniforms() {
        glUniform1i(locationBorderWidth, element.borderWidth)
        glUniform1i(locationBorderColor, element.borderColor)
    }
}
