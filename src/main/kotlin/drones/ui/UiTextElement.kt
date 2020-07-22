package drones.ui

import drones.GameFont
import org.joml.Vector2f
import org.joml.Vector2fc

class UiTextElement(override val font: GameFont, override var string: String = "",
                    override val autoDimensions: LayoutVectorc = LayoutVector.ZERO) : UiElement(), UiTextParams {
    private val mMinDimensions = Vector2f()
    override val minDimensions: Vector2fc = mMinDimensions

    override val provideRenderer: (UiGraphicsManager) -> UiElementRenderer = UiGraphicsManager::textRenderer
    override val children: List<UiLayout> = emptyList()

    var fontSize: Int = font.size
    override var fontScale: Float
        get() = 1.0f * fontSize / font.size
        set(value) { fontSize = (font.size * value).toInt() }

    override var textFgColor: Int = 15
    override var textBgColor: Int = 0
    override var textAlign: TextAlign = TextAlign.LEFT_ALIGN
    override var transparentBg: Boolean = true
    var lineSpacing: Float = 1.0f

    override fun computeAutoMeasurements() {
        var width = 0f
        for (ch: Char in string) {
            val charCode: Int = font.characterCodeLut[ch] ?: error("Font does not suppor character '$ch'")
            val charWidth: Int = font.characterWidthLut[charCode]
            width += (charWidth + 1) * fontScale
        }

        mMinDimensions.set(width, fontScale * font.height * lineSpacing)
        mMinDimensions.max(minDimensions)
    }

    override fun computeFinalMeasurements() { /* nothing to do, no children */ }

    enum class TextAlign(val id: Int) {
        LEFT_ALIGN(0),
        RIGHT_ALIGN(1),
        CENTER_ALIGN(2)
    }
}
