package drones.ui

import drones.GameFont
import org.joml.Vector2f
import org.joml.Vector2fc

class UiTextElement(layout: UiLayout, parentLayout: UiLayout,
                    val font: GameFont) : UiElement(layout, parentLayout) {
    var string: String = ""
        set(value) {
            field = value
            updateDimensions()
        }

    var fontSize: Int = font.lineHeight
    val fontScale: Float get() = 1.0f * fontSize / font.lineHeight

    private val mutableDimensions = Vector2f(0f, 1.0f * fontSize)
    override val autoDimensions: Vector2fc = mutableDimensions

    override var renderer: UiTextRenderer? = null

    private fun updateDimensions() {
        var width = 0f
        for (ch: Char in string) {
            val charCode: Int = font.characterCodeLut[ch] ?: error("Font does not suppor character '$ch'")
            val charWidth: Int = font.characterWidthLut[charCode]
            width += charWidth * fontScale
        }

        mutableDimensions.set(width, 1.0f * fontSize)
    }
}
