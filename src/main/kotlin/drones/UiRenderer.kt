package drones

import org.lwjgl.opengl.GL30.*
import org.lwjgl.opengl.GL43.GL_SHADER_STORAGE_BUFFER

class UiRenderer(private val ui: Ui, private val shaderProgram: Int,
                 private val ssbo: Int, private val font: GameFont) : Renderer {
    private val uniformWindowSize: Int
    private val uniformUiAnchorPoint: Int
    private val uniformUiPositionPx: Int
    private val uniformUiDimensionsPx: Int
    private val uniformFontScale: Int
    private val uniformFontSpacing: Int
    private val uniformFontAlign: Int
    private val uniformFontTransparentBg: Int

    private val vao: Int

    init {
        uniformWindowSize = glGetUniformLocation(shaderProgram, "WindowSize")
        uniformUiAnchorPoint = glGetUniformLocation(shaderProgram, "UiAnchorPoint")
        uniformUiPositionPx = glGetUniformLocation(shaderProgram, "UiPositionPx")
        uniformUiDimensionsPx = glGetUniformLocation(shaderProgram, "UiDimensionsPx")
        uniformFontScale = glGetUniformLocation(shaderProgram, "FontScale")
        uniformFontSpacing = glGetUniformLocation(shaderProgram, "FontSpacing")
        uniformFontAlign = glGetUniformLocation(shaderProgram, "FontAlign")
        uniformFontTransparentBg = glGetUniformLocation(shaderProgram, "FontTransparentBg")

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

    override fun render(cameraMatrixArr: FloatArray, time: Float) {
        glUseProgram(shaderProgram)
        glUniform2f(uniformWindowSize, ui.screenWidth, ui.screenHeight)
        glUniform2f(uniformUiAnchorPoint, ui.anchorPoint.x(), ui.anchorPoint.y())
        glUniform2f(uniformUiPositionPx, ui.position.x(), ui.position.y())
        glUniform2f(uniformUiDimensionsPx, ui.dimensions.x(), ui.dimensions.y())
        glUniform1f(uniformFontScale, 2f)
        glUniform1f(uniformFontSpacing, 12f)
        glUniform1i(uniformFontAlign, ui.textAlign.id)
        glUniform1i(uniformFontTransparentBg, if (ui.transparentTextBg) 1 else 0)

        ui.requestedString?.let { str ->
            val arr = stringToBitmapArray(str, font, ui.textBgColor, ui.textFgColor)

            glBindBuffer(GL_SHADER_STORAGE_BUFFER, ssbo)
            glBufferSubData(GL_SHADER_STORAGE_BUFFER, 1096, arr)
            glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0)
        }

        glBindVertexArray(vao)
        glBindTexture(GL_TEXTURE_2D, font.glBitmap)
        glDrawArrays(GL_TRIANGLES, 0, 6)
    }
}
