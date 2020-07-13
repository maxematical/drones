package drones.render

import drones.game.GameObject
import org.joml.Vector2fc
import org.lwjgl.opengl.GL30.*

abstract class GameObjectRenderer(private val obj: GameObject, private val shaderProgram: Int) :
    Renderer {
    private val vao: Int

    private val locationCameraMatrix: Int
    private val locationModelMatrix: Int
    private val locationTime: Int

    init {
        locationCameraMatrix = glGetUniformLocation(shaderProgram, "cameraMatrix")
        locationModelMatrix = glGetUniformLocation(shaderProgram, "modelMatrix")
        locationTime = glGetUniformLocation(shaderProgram, "Time")

        val vertices: FloatArray = floatArrayOf(
            -0.5f, -0.5f, 0.0f, 0f, 1f,
            0.5f, -0.5f, 0.0f, 1f, 1f,
            0.5f, 0.5f, 0.0f, 1f, 0f,

            -0.5f, 0.5f, 0f, 0f, 0f,
            -0.5f, -0.5f, 0f, 0f, 1f,
            0.5f, 0.5f, 0f, 1f, 0f
        )

        vao = glGenVertexArrays()
        glBindVertexArray(vao)

        val vbo = glGenBuffers()
        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 20, 0)
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 20, 12)
        glEnableVertexAttribArray(0)
        glEnableVertexAttribArray(1)
    }

    final override fun render(screenDimensions: Vector2fc, cameraMatrixArr: FloatArray, time: Float) {
        glUseProgram(shaderProgram)
        glUniformMatrix4fv(locationCameraMatrix, false, cameraMatrixArr)
        glUniformMatrix4fv(locationModelMatrix, false, obj.modelMatrixArr)
        glUniform1f(locationTime, time - obj.spawnedTime)
        setUniforms()

        glBindVertexArray(vao)
        glDrawArrays(GL_TRIANGLES, 0, 6)
    }

    open fun setUniforms() {}
}
