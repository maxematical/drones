package drones.ui

import drones.GameFont
import org.joml.Vector2f
import org.joml.Vector2fc

class UiTextArea(override val font: GameFont, override val autoDimensions: LayoutVectorc = LayoutVector.ZERO,
                 private val minLines: Int = 0) : UiElement(), UiTextAreaParams {
    override val provideRenderer = UiGraphicsManager::textAreaRenderer

    private val mMinDimensions = Vector2f()
    override val minDimensions: Vector2fc = mMinDimensions

    override val children: List<UiLayout> = emptyList()

    override val lines: MutableList<String> = mutableListOf()

    var fontSize: Int = font.size
    override var fontScale: Float
        get() = 1.0f * fontSize / font.size
        set(value) { fontSize = (font.size * value).toInt() }

    override var lineFgColors: IntArray = intArrayOf(15)
    override var lineBgColors: IntArray = intArrayOf(0)
    override var transparentBg: Boolean = true
    override var lineSpacing: Float = 1.0f

    override var borderWidth: Int = 2
    override var borderColor: Int = 0xFFFFFF
    override var backgroundColor: Int = 0x000000
    override val padding: Padding = Padding(3f)

    var allowOverflowX: Boolean = false
    var allowOverflowY: Boolean = true

    override fun computeAutoMeasurements() {
        val numberLines = lines.size
        val maxLineLength = lines.maxBy { it.length }?.length ?: 0

        val numberVisibleLines = if (allowOverflowY) Math.max(numberLines, minLines) else minLines

        val width = maxLineLength * fontScale * (font.characterWidthLut[0] + 1) + padding.totalHorizontal
        val height = numberVisibleLines * fontScale * font.height * lineSpacing + padding.totalVertical

        mMinDimensions.set(if (allowOverflowX) width else 0f, height)
    }

    override fun computeFinalMeasurements() {
        // No children, no need to do anything
    }

    fun setLines(string: String) {
        lines.clear()
        lines.addAll(string.split('\n'))
    }
}
