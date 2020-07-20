package drones.ui

import drones.GameFont
import org.joml.Vector2f
import org.joml.Vector2fc

class UiTextArea(val font: GameFont, override val autoDimensions: LayoutVectorc = LayoutVector(),
                 private val minLines: Int = 0) : UiElement() {
    override var renderer: UiTextAreaRenderer? = null

    private val mMinDimensions = Vector2f()
    override val minDimensions: Vector2fc = mMinDimensions

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

    var textFgColor: IntArray = intArrayOf(15)
    var textBgColor: IntArray = intArrayOf(0)
    var transparentBg: Boolean = true
    var lineSpacing: Float = 1.0f

    var borderWidth: Int = 2
    var borderColor: Int = 0xFFFFFF
    var backgroundColor: Int = 0x000000
    val textPadding: Padding = Padding(3f)

    var allowOverflowX: Boolean = false
    var allowOverflowY: Boolean = true

    override fun computeAutoMeasurements() {
        val lines = string.split('\n')
        val numberLines = lines.size
        val maxLineLength = lines.maxBy { it.length }!!.length

        val width = maxLineLength * fontScale * (font.characterWidthLut[0] + 1) + textPadding.totalHorizontal
        val height = Math.max(numberLines, minLines) * fontScale * font.height * lineSpacing + textPadding.totalVertical

        mMinDimensions.set(if (allowOverflowX) width else 0f, if (allowOverflowY) height else 0f)
    }

    override fun computeFinalMeasurements() {
        // No children, no need to do anything
    }
}
