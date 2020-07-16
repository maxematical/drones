package drones.ui

import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL43.*

class UiTextRenderer(private val element: UiTextElement, shaderProgram: Int,
                     private val ssbo: Int) : UiRenderer(element, shaderProgram) {
    private val stringLengthArr = IntArray(1)
    private val textData = IntArray(256)

    private val locationFontScale: Int
    private val locationLetterSpacing: Int
    private val locationTextAlign: Int
    private val locationTransparentBg: Int
    private val locationLineHeight: Int

    init {
        locationFontScale = glGetUniformLocation(shaderProgram, "TextFontScale")
        locationLetterSpacing = glGetUniformLocation(shaderProgram, "TextLetterSpacing")
        locationTextAlign = glGetUniformLocation(shaderProgram, "TextAlign")
        locationTransparentBg = glGetUniformLocation(shaderProgram, "TextTransparentBg")
        locationLineHeight = glGetUniformLocation(shaderProgram, "TextLineHeight")
    }

    override fun setUniforms() {
        updateTextData()

        glBindBuffer(GL_SHADER_STORAGE_BUFFER, ssbo)
        glBufferSubData(GL_SHADER_STORAGE_BUFFER, 1096, stringLengthArr)
        glBufferSubData(GL_SHADER_STORAGE_BUFFER, 1100, textData)

        glUniform1f(locationFontScale, element.fontScale)
        glUniform1f(locationLetterSpacing, (element.font.characterWidthLut[0].toFloat()+1) * element.fontScale)
        glUniform1i(locationTextAlign, element.textAlign.id)
        glUniform1i(locationTransparentBg, if (element.transparentBg) GL_TRUE else GL_FALSE)
        glUniform1i(locationLineHeight, element.fontSize)

        glBindTexture(GL_TEXTURE_2D, element.font.glBitmap)
    }

    private fun updateTextData() {
        stringLengthArr[0] = element.string.length

        var index = 0
        for (char: Char in element.string) {
            val charCode: Int = element.font.characterCodeLut[char] ?: error("Font does not have character '$char'")
            val data = (charCode and 255) or (element.textBgColor shl 8) or (element.textFgColor shl 16)
            textData[index++] = data
        }
    }
}
