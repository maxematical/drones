package drones.ui

import org.joml.Vector2f
import org.joml.Vector2fc
import org.lwjgl.opengl.GL43

class UiBaseRenderer(private val shaderProgram: Int) {
    private val vao: Int

    private val locationQuadMatrix: Int
    private val locationScreenDimensions: Int
    private val locationElementDimensions: Int
    private val locationElementPosition: Int

    init {
        locationQuadMatrix = GL43.glGetUniformLocation(shaderProgram, "QuadMatrix")
        locationScreenDimensions = GL43.glGetUniformLocation(shaderProgram, "ScreenDimensions")
        locationElementDimensions = GL43.glGetUniformLocation(shaderProgram, "ElementDimensions")
        locationElementPosition = GL43.glGetUniformLocation(shaderProgram, "ElementPosition")

        vao = GL43.glGenVertexArrays()
        GL43.glBindVertexArray(vao)
        val vbo = GL43.glGenBuffers()
        GL43.glBindBuffer(GL43.GL_ARRAY_BUFFER, vbo)
        GL43.glBufferData(GL43.GL_ARRAY_BUFFER, quadVertices, GL43.GL_STATIC_DRAW)
        GL43.glVertexAttribPointer(0, 3, GL43.GL_FLOAT, false, 20, 0)
        GL43.glVertexAttribPointer(1, 2, GL43.GL_FLOAT, false, 20, 12)
        GL43.glEnableVertexAttribArray(0)
        GL43.glEnableVertexAttribArray(1)
    }

    fun <T : UiBaseParams> render(screenDimensions: Vector2fc, params: T, setUniforms: (T) -> Unit) {
        GL43.glUseProgram(shaderProgram)
        GL43.glUniformMatrix4fv(locationQuadMatrix, false, params.quadMatrixArr)
        GL43.glUniform2f(locationScreenDimensions, screenDimensions.x(), screenDimensions.y())
        GL43.glUniform2f(locationElementDimensions, params.computedDimensions.x(), params.computedDimensions.y())
        GL43.glUniform2f(locationElementPosition, params.computedPosition.x(), params.computedPosition.y())

        setUniforms(params)

        GL43.glBindVertexArray(vao)
        GL43.glDrawArrays(GL43.GL_TRIANGLES, 0, 6)
    }

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

interface UiBaseParams {
    val computedDimensions: Vector2f
    val computedPosition: Vector2f
    val quadMatrixArr: FloatArray
}
