package drones.ui

import org.lwjgl.opengl.GL43

class UiTextRenderer(private val element: UiTextElement, shaderProgram: Int) : UiRenderer(element, shaderProgram) {
    private val stringLengthArr = IntArray(1)
    private val textData = IntArray(256)

    override fun setUniforms() {
        updateTextData()
        GL43.glBufferSubData(0, 1096, stringLengthArr)
        GL43.glBufferSubData(0, 1100, textData)

        GL43.glBindTexture(GL43.GL_TEXTURE_2D, element.font.glBitmap)
    }

    private fun updateTextData() {
        stringLengthArr[0] = element.string.length

        var index = 0
        for (char: Char in element.string) {
            val charCode: Int = element.font.characterCodeLut[char] ?: error("Font does not have character '$char'")
            val data = (charCode and 255) or (0 shl 8) or (15 shl 16)
            textData[index++] = data
        }
    }
}
