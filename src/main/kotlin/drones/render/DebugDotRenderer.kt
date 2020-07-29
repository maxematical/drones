package drones.render

import org.joml.Vector2fc
import org.lwjgl.opengl.GL30.*

class DebugDotRenderer(private val shaderProgram: Int,
                       var debugPosition: Vector2fc, var debugRadius: Float = 4f) : Renderer {
    private val vao: Int

    init {
        val quadVertices: FloatArray = floatArrayOf(
            -1f, -1f, 0.0f,
            1f, -1f, 0.0f,
            1f, 1f, 0.0f,

            -1f, 1f, 0f,
            -1f, -1f, 0f,
            1f, 1f, 0f
        )
        vao = glGenVertexArrays()
        glBindVertexArray(vao)
        val vbo = glGenBuffers()
        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glBufferData(GL_ARRAY_BUFFER, quadVertices, GL_STATIC_DRAW)
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 12, 0)
        glEnableVertexAttribArray(0)
    }

    override fun render(screenDimensions: Vector2fc, cameraMatrixArr: FloatArray, time: Float, drawTime: Float) {
        glBindVertexArray(vao)
        glUseProgram(shaderProgram)
        glUniform2f(glGetUniformLocation(shaderProgram, "DebugPosition"), debugPosition.x(), debugPosition.y())
        glUniform1f(glGetUniformLocation(shaderProgram, "DebugRadius"), debugRadius)
        glDrawArrays(GL_TRIANGLES, 0, 6)
    }
}
