package drones.ui

import drones.GameFont
import drones.stringToBitmapArray
import org.lwjgl.opengl.GL43.*

class UiTextRenderer(private val ui: UiText, shaderProgram: Int,
                     private val ssbo: Int, private val font: GameFont
) : UiRenderer(ui, shaderProgram) {
    private val uniformFontScale: Int
    private val uniformFontSpacing: Int
    private val uniformFontAlign: Int
    private val uniformFontTransparentBg: Int

    init {
        uniformFontScale = glGetUniformLocation(shaderProgram, "FontScale")
        uniformFontSpacing = glGetUniformLocation(shaderProgram, "FontSpacing")
        uniformFontAlign = glGetUniformLocation(shaderProgram, "FontAlign")
        uniformFontTransparentBg = glGetUniformLocation(shaderProgram, "FontTransparentBg")
    }

    override fun preRender() {
        glUniform1f(uniformFontScale, ui.fontScale)
        glUniform1f(uniformFontSpacing, ui.fontSpacing)
        glUniform1i(uniformFontAlign, ui.textAlign.id)
        glUniform1i(uniformFontTransparentBg, if (ui.transparentTextBg) 1 else 0)

        ui.requestedString?.let { str ->
            val arr = stringToBitmapArray(str, font, ui.textBgColor, ui.textFgColor)

            glBindBuffer(GL_SHADER_STORAGE_BUFFER, ssbo)
            glBufferSubData(GL_SHADER_STORAGE_BUFFER, 1096, arr)
            glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0)
        }

        glBindTexture(GL_TEXTURE_2D, font.glBitmap)
    }
}
