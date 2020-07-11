package drones.ui

import org.lwjgl.opengl.GL30.*

class UiBoxRenderer(private val ui: UiBox, shaderProgram: Int) : UiRenderer(ui, shaderProgram) {
    private val locationBorderSize: Int
    private val locationBorderColor: Int

    init {
        locationBorderSize = glGetUniformLocation(shaderProgram, "BoxBorderSize")
        locationBorderColor = glGetUniformLocation(shaderProgram, "BoxBorderColor")
    }

    override fun preRender() {
        glUniform1f(locationBorderSize, ui.borderSize)
        glUniform1i(locationBorderColor, ui.borderColor)
    }
}
