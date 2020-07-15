package drones.ui

import drones.GameFont
import org.joml.Vector2f
import org.joml.Vector2fc

class UiTextElement(val font: GameFont, var string: String = "") : UiElement() {

    var fontSize: Int = font.lineHeight
    var fontScale: Float
        get() = 1.0f * fontSize / font.lineHeight
        set(value) { fontSize = (font.lineHeight * value).toInt() }

    var textFgColor: Int = 15
    var textBgColor: Int = 0

    var transparentBg: Boolean = false

    private val mutableDimensions = Vector2f(0f, 1.0f * fontSize)
    override val autoDimensions: Vector2fc = mutableDimensions

    override var renderer: UiTextRenderer? = null
    override val children: List<UiLayout> = emptyList()

    override fun computeChildMeasurements() {
        updateDimensions()
    }

    private fun updateDimensions() {
        var width = 0f
        for (ch: Char in string) {
            val charCode: Int = font.characterCodeLut[ch] ?: error("Font does not suppor character '$ch'")
            val charWidth: Int = font.characterWidthLut[charCode]
            width += charWidth * fontScale
        }

        mutableDimensions.set(width, fontSize.toFloat())
    }
}
