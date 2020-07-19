package drones.ui

import drones.GameFont
import org.joml.Vector2f
import org.joml.Vector2fc

class UiTextArea(val font: GameFont, private val minDimensions: Vector2fc = Vector2f()) : UiElement() {
    override var renderer: UiTextAreaRenderer? = null

    private val mutAutoDimensions = Vector2f()
    override val autoDimensions: Vector2fc = mutAutoDimensions
    override val children: List<UiLayout> = emptyList()

    var string: String = ""
        set(value) {
            field = value
            this.lines = value.split('\n')
        }
    var lines: List<String> = listOf()
        private set

    var fontSize: Int = font.size
    var fontScale: Float
        get() = 1.0f * fontSize / font.size
        set(value) { fontSize = (font.size * value).toInt() }

    var textFgColor: Int = 15
    var textBgColor: Int = 0
    var transparentBg: Boolean = true
    var lineSpacing: Float = 1.0f

    var borderWidth: Int = 2
    var borderColor: Int = 0xFFFFFF
    var backgroundColor: Int = 0x000000
    val textPadding: Padding = Padding(3f)

    override fun computeChildMeasurements() {
        val lines = string.split('\n')
        val numberLines = lines.size
        val maxLineLength = lines.maxBy { it.length }!!.length

        val width = maxLineLength * fontScale * (font.characterWidthLut[0] + 1) + textPadding.totalHorizontal
        val height = numberLines * fontScale * font.height * lineSpacing + textPadding.totalVertical

        mutAutoDimensions.set(width, height)
        mutAutoDimensions.max(minDimensions)
    }
}
