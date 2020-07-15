package drones.ui

import org.joml.Vector2fc
import org.lwjgl.opengl.GL43.*

abstract class UiRenderer(private val element: UiElement, private val shaderProgram: Int) {
    private val vao: Int

    private val locationQuadMatrix: Int
    private val locationScreenDimensions: Int
    private val locationElementDimensions: Int
    private val locationElementPosition: Int

    init {
        locationQuadMatrix = glGetUniformLocation(shaderProgram, "QuadMatrix")
        locationScreenDimensions = glGetUniformLocation(shaderProgram, "ScreenDimensions")
        locationElementDimensions = glGetUniformLocation(shaderProgram, "ElementDimensions")
        locationElementPosition = glGetUniformLocation(shaderProgram, "ElementPosition")

        vao = glGenVertexArrays()
        glBindVertexArray(vao)
        val vbo = glGenBuffers()
        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glBufferData(GL_ARRAY_BUFFER, quadVertices, GL_STATIC_DRAW)
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 20, 0)
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 20, 12)
        glEnableVertexAttribArray(0)
        glEnableVertexAttribArray(1)
    }

    fun render(screenDimensions: Vector2fc) {
        element.recomputeMatrices(screenDimensions)

        glUseProgram(shaderProgram)
        glUniformMatrix4fv(locationQuadMatrix, false, element.quadMatrixArr)
        glUniform2f(locationScreenDimensions, screenDimensions.x(), screenDimensions.y())
        glUniform2f(locationElementDimensions, element.computedDimensions.x(), element.computedDimensions.y())
        glUniform2f(locationElementPosition, element.computedPosition.x(), element.computedPosition.y())

        setUniforms()

        glBindVertexArray(vao)
        glDrawArrays(GL_TRIANGLES, 0, 6)
    }

    open fun setUniforms() {}

    private companion object {
        //   X,   Y,  Z,    u,  v
        val quadVertices = floatArrayOf(
            0f, -1f, 0f,   0f, 0f,
            1f, -1f, 0f,   1f, 0f,
            1f,  0f, 0f,   1f, 1f,

            0f, -1f, 0f,   0f, 0f,
            1f,  0f, 0f,   1f, 1f,
            0f,  0f, 0f,   0f, 1f
        )
    }
}
