package drones.ui

import drones.GameFont
import org.joml.Vector2fc
import org.lwjgl.opengl.GL43.*

class UiTextRenderer(graphicsManager: GraphicsManager) : UiRenderer<UiTextParams>, UiElementRenderer {
    private val shaderProgram = graphicsManager.textShaderProgram
    private val ssbo = graphicsManager.ssbo

    private val baseRenderer = UiBaseRenderer(shaderProgram)

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

    override fun render(element: UiElement, screenDimensions: Vector2fc) {
        render(screenDimensions, element as UiTextElement)
    }

    override fun render(screenDimensions: Vector2fc, params: UiTextParams) {
        baseRenderer.render(screenDimensions, params, this::setUniforms)
    }

    private fun setUniforms(params: UiTextParams) {
        val scaledLetterSpacing = (params.font.characterWidthLut[0].toFloat()+1) * params.fontScale

        // Sometimes part of the text will get cut off because the text element is too small
        val maxVisibleChars = Math.floor(1.0 * params.computedDimensions.x() / scaledLetterSpacing).toInt()

        updateTextData(params, maxVisibleChars)

        glBindBuffer(GL_SHADER_STORAGE_BUFFER, ssbo)
        glBufferSubData(GL_SHADER_STORAGE_BUFFER, 1096, stringLengthArr)
        glBufferSubData(GL_SHADER_STORAGE_BUFFER, 1100, textData)

        glUniform1f(locationFontScale, params.fontScale)
        glUniform1f(locationLetterSpacing, scaledLetterSpacing)
        glUniform1i(locationTextAlign, params.textAlign.id)
        glUniform1i(locationTransparentBg, if (params.transparentBg) GL_TRUE else GL_FALSE)
        glUniform1i(locationLineHeight, params.font.height)

        glBindTexture(GL_TEXTURE_2D, params.font.glBitmap)
    }

    private fun updateTextData(params: UiTextParams, maxVisibleChars: Int) {
        stringLengthArr[0] = Math.min(params.string.length, maxVisibleChars)

        for (index in 0 until stringLengthArr[0]) {
            var char = params.string[index]

            // If the text is cut off (there isn't enough room to show all characters), put the last character as an
            // ellipsis instead
            val isTextCutOff = maxVisibleChars < params.string.length
            val isLastCharacter = index == (stringLengthArr[0] - 1)
            if (isTextCutOff && isLastCharacter)
                char = '\u2026' // ellipsis

            val charCode: Int = params.font.characterCodeLut[char] ?: error("Font does not have character '$char'")
            val data = (charCode and 255) or (params.textBgColor shl 8) or (params.textFgColor shl 16)
            textData[index] = data
        }
    }
}

interface UiHasTextParams : UiBaseParams {
    val font: GameFont
    val fontScale: Float
    val transparentBg: Boolean
}

interface UiTextParams : UiHasTextParams {
    val string: String
    val textAlign: UiTextElement.TextAlign
    val textFgColor: Int
    val textBgColor: Int
}
