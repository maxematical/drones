package drones.ui

import org.lwjgl.opengl.GL43.*

class UiTextRenderer(private val element: UiTextElement, shaderProgram: Int,
                     private val ssbo: Int) : UiRenderer(element, shaderProgram) {
    private val stringLengthArr = IntArray(1)
    private val textData = IntArray(256)

    override fun setUniforms() {
        updateTextData()

        glBindBuffer(GL_SHADER_STORAGE_BUFFER, ssbo)
        glBufferSubData(0, 1096, stringLengthArr)
        glBufferSubData(0, 1100, textData)

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
