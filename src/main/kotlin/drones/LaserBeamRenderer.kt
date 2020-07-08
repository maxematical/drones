package drones

import org.joml.Matrix4f
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30
import org.lwjgl.opengl.GL43.*

class LaserBeamRenderer(private val laserBeam: LaserBeam) {
    private val vao: Int

    private val modelMatrix: Matrix4f = Matrix4f()
    private val modelMatrixArr: FloatArray = FloatArray(16)

    private val initTime: Long = System.currentTimeMillis()

    init {
        val vertices: FloatArray = floatArrayOf(
            -0.5f, -0.5f, 0.0f,     0f, 1f,
            0.5f, -0.5f, 0.0f,      1f, 1f,
            0.5f, 0.5f, 0.0f,       1f, 0f,

            -0.5f, 0.5f, 0f,        0f, 0f,
            -0.5f, -0.5f, 0f,       0f, 1f,
            0.5f, 0.5f, 0f,         1f, 0f
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

    fun render(shaderProgram: Int, cameraMatrixArr: FloatArray) {
        updateModelMatrix()

        glUseProgram(shaderProgram)
        glUniformMatrix4fv(glGetUniformLocation(shaderProgram, "cameraMatrix"), false, cameraMatrixArr)
        glUniformMatrix4fv(glGetUniformLocation(shaderProgram, "modelMatrix"), false, modelMatrixArr)
        glUniform2f(glGetUniformLocation(shaderProgram, "LaserDimensions"), laserBeam.length, laserBeam.width)
        glUniform1f(glGetUniformLocation(shaderProgram, "Time"), (System.currentTimeMillis() - initTime) * 0.001f)

        glBindVertexArray(vao)
        glDrawArrays(GL_TRIANGLES, 0, 6)
    }

    private fun updateModelMatrix() {
        modelMatrix.identity()
            .translate(laserBeam.position.x, laserBeam.position.y, 0f)
            .rotate(laserBeam.rotation * MathUtils.DEG2RAD, 0f, 0f, 1f)
            .translate(laserBeam.length * 0.5f, 0f, 0f)
            .scale(laserBeam.length, laserBeam.width, 1f)
        modelMatrix.get(modelMatrixArr)
    }
}
