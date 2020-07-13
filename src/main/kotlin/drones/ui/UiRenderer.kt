package drones.ui

import drones.render.Renderer
import org.joml.Vector2fc
import org.lwjgl.opengl.GL30.*

abstract class UiRenderer(private val ui: Ui, private val shaderProgram: Int) : Renderer {
    private val uniformWindowSize: Int
    private val uniformUiAnchorPoint: Int
    private val uniformUiPositionPx: Int
    private val uniformUiDimensionsPx: Int

    private val vao: Int

    init {
        uniformWindowSize = glGetUniformLocation(shaderProgram, "WindowSize")
        uniformUiAnchorPoint = glGetUniformLocation(shaderProgram, "UiAnchorPoint")
        uniformUiPositionPx = glGetUniformLocation(shaderProgram, "UiPositionPx")
        uniformUiDimensionsPx = glGetUniformLocation(shaderProgram, "UiDimensionsPx")

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

    final override fun render(screenDimensions: Vector2fc, cameraMatrixArr: FloatArray, time: Float) {
        if (!ui.shown)
            return

        glUseProgram(shaderProgram)
        glUniform2f(uniformWindowSize, screenDimensions.x(), screenDimensions.y())
        glUniform2f(uniformUiAnchorPoint, ui.anchorPoint.x(), ui.anchorPoint.y())
        glUniform2f(uniformUiPositionPx, ui.position.x(), ui.position.y())
        glUniform2f(uniformUiDimensionsPx, ui.dimensions.x(), ui.dimensions.y())

        preRender()

        glBindVertexArray(vao)
        glDrawArrays(GL_TRIANGLES, 0, 6)

        for (child in ui.children) {
            child.renderer?.render(screenDimensions, cameraMatrixArr, time)
        }
    }

    /** Called after the shader program is bound, but before the quad is drawn. */
    open fun preRender() {}
}
