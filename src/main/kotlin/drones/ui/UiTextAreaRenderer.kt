package drones.ui

import org.joml.Matrix4f
import org.joml.Vector2fc
import org.lwjgl.opengl.GL43

class UiTextAreaRenderer(private val element: UiTextArea,
                         private val ssbo: Int,
                         private val boxShaderProgram: Int,
                         private val textShaderProgram: Int) : UiRenderer(element, boxShaderProgram) {
    private val locationFontScale: Int
    private val locationLetterSpacing: Int
    private val locationTextAlign: Int
    private val locationTransparentBg: Int
    private val locationLineHeight: Int

    private val locationBorderWidth: Int
    private val locationBorderColor: Int
    private val locationBackgroundColor: Int

    private val locationTextQuadMatrix: Int
    private val locationTextScreenDimensions: Int
    private val locationTextElementDimensions: Int
    private val locationTextElementPosition: Int

    private val textMatrix = Matrix4f()
    private val textMatrixArr = FloatArray(16)

    init {
        locationFontScale = GL43.glGetUniformLocation(textShaderProgram, "TextFontScale")
        locationLetterSpacing = GL43.glGetUniformLocation(textShaderProgram, "TextLetterSpacing")
        locationTextAlign = GL43.glGetUniformLocation(textShaderProgram, "TextAlign")
        locationTransparentBg = GL43.glGetUniformLocation(textShaderProgram, "TextTransparentBg")
        locationLineHeight = GL43.glGetUniformLocation(textShaderProgram, "TextLineHeight")

        locationBorderWidth = GL43.glGetUniformLocation(boxShaderProgram, "BoxBorderSize")
        locationBorderColor = GL43.glGetUniformLocation(boxShaderProgram, "BoxBorderColor")
        locationBackgroundColor = GL43.glGetUniformLocation(boxShaderProgram, "BoxBackgroundColor")

        locationTextQuadMatrix = GL43.glGetUniformLocation(textShaderProgram, "QuadMatrix")
        locationTextScreenDimensions = GL43.glGetUniformLocation(textShaderProgram, "ScreenDimensions")
        locationTextElementDimensions = GL43.glGetUniformLocation(textShaderProgram, "ElementDimensions")
        locationTextElementPosition = GL43.glGetUniformLocation(textShaderProgram, "ElementPosition")
    }

    override fun render(screenDimensions: Vector2fc) {
        super.render(screenDimensions)

        for (i in element.lines.indices) {
            renderLine(screenDimensions, i, element.lines[i])
        }
    }

    private fun renderLine(screenDimensions: Vector2fc, lineIndex: Int, line: String) {
        // Compute matrix to transform the quad to the desired text coordinates
        val scaledLineSpacing = element.lineSpacing * element.fontScale * element.font.height

        val textWidth = element.computedDimensions.x() - element.textPadding.totalHorizontal
        val textHeight = element.fontScale * element.font.height

        val posX = element.computedPosition.x() + element.textPadding.left
        val posY = element.computedPosition.y() - element.textPadding.top - lineIndex * scaledLineSpacing

        // Don't show lines after the cutoff -- TODO use proper masking here
        if (element.computedPosition.y() - (posY - textHeight) > element.computedDimensions.y()) {
            return
        }

        textMatrix
            .identity()
            .translate(-1f, -1f, 0f)
            .scale(2f / screenDimensions.x(), 2f / screenDimensions.y(), 0f)
            .translate(posX, posY, 0f)
            .scale(textWidth, textHeight, 0f)
        textMatrix.get(textMatrixArr)

        // Load default uniforms
        GL43.glUseProgram(textShaderProgram)
        GL43.glUniformMatrix4fv(locationTextQuadMatrix, false, textMatrixArr)
        GL43.glUniform2f(locationTextScreenDimensions, screenDimensions.x(), screenDimensions.y())
        GL43.glUniform2f(locationTextElementDimensions, textWidth, textHeight)
        GL43.glUniform2f(locationTextElementPosition, posX, posY)

        // Load text uniforms
        GL43.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, ssbo)
        GL43.glBufferSubData(GL43.GL_SHADER_STORAGE_BUFFER, 1096, intArrayOf(line.length))
        GL43.glBufferSubData(GL43.GL_SHADER_STORAGE_BUFFER, 1100, charDataArray(line, lineIndex))

        GL43.glUniform1f(locationFontScale, element.fontScale)
        GL43.glUniform1f(locationLetterSpacing, (element.font.characterWidthLut[0].toFloat() + 1) * element.fontScale)
        GL43.glUniform1i(locationTextAlign, 0)
        GL43.glUniform1i(locationTransparentBg, if (element.transparentBg) GL43.GL_TRUE else GL43.GL_FALSE)
        GL43.glUniform1i(locationLineHeight, element.font.height)

        // Draw the quad
        GL43.glBindVertexArray(vao)
        GL43.glDrawArrays(GL43.GL_TRIANGLES, 0, 6)
    }

    override fun setUniforms() {
        GL43.glUniform1i(locationBorderWidth, element.borderWidth)
        GL43.glUniform1i(locationBorderColor, element.borderColor)
        GL43.glUniform1i(locationBackgroundColor, element.backgroundColor)
    }

    private fun charDataArray(string: String, lineIndex: Int): IntArray {
        val arr = IntArray(string.length)

        var index = 0
        for (char: Char in string) {
            val bgColor = element.textBgColor[lineIndex % element.textBgColor.size]
            val fgColor = element.textFgColor[lineIndex % element.textFgColor.size]

            val charCode: Int = element.font.characterCodeLut[char] ?: error("Font does not have character '$char'")
            val data = (charCode and 255) or (bgColor shl 8) or (fgColor shl 16)
            arr[index++] = data
        }

        return arr
    }
}
