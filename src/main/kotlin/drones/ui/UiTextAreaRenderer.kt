package drones.ui

import drones.GameFont
import drones.ui.UiTextElement.TextAlign.LEFT_ALIGN
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector2fc

class UiTextAreaRenderer(graphicsManager: GraphicsManager) : UiRenderer<UiTextAreaParams>, UiElementRenderer {
    private val boxRenderer = UiBoxRenderer(graphicsManager)
    private val textRenderer = UiTextRenderer(graphicsManager)

    private var textParams: MutableTextParams? = null
    private val textMatrix = Matrix4f()

    override fun render(element: UiElement, screenDimensions: Vector2fc) {
        render(screenDimensions, element as UiTextArea)
    }

    override fun render(screenDimensions: Vector2fc, params: UiTextAreaParams) {
        boxRenderer.render(screenDimensions, params)

        for (i in params.lines.indices) {
            renderLine(params, screenDimensions, i, params.lines[i])
        }
    }

    private fun renderLine(params: UiTextAreaParams, screenDimensions: Vector2fc, lineIndex: Int, line: String) {
        // Compute matrix to transform the quad to the desired text coordinates
        val scaledLineSpacing = params.lineSpacing * params.fontScale * params.font.height

        val textWidth = params.computedDimensions.x() - params.padding.totalHorizontal
        val textHeight = params.fontScale * params.font.height

        val posX = params.computedPosition.x() + params.padding.left
        val posY = params.computedPosition.y() - params.padding.top - lineIndex * scaledLineSpacing

        // Don't show lines after the cutoff -- TODO use proper masking here
        if (params.computedPosition.y() - (posY - textHeight) > params.computedDimensions.y()) {
            return
        }

        textMatrix
            .identity()
            .translate(-1f, -1f, 0f)
            .scale(2f / screenDimensions.x(), 2f / screenDimensions.y(), 0f)
            .translate(posX, posY, 0f)
            .scale(textWidth, textHeight, 0f)

        // Determine the fg/bg color for this line
        val fgColor = params.lineFgColors[lineIndex % params.lineFgColors.size]
        val bgColor = params.lineBgColors[lineIndex % params.lineBgColors.size]

        // Update text params
        var textParams = this.textParams
        if (textParams == null) {
            textParams = MutableTextParams(params.font, params.fontScale, params.transparentBg,
                line, LEFT_ALIGN, fgColor, bgColor)
        } else {
            textParams.font = params.font
            textParams.fontScale = params.fontScale
            textParams.transparentBg = params.transparentBg
            textParams.string = line
            textParams.textAlign = LEFT_ALIGN
            textParams.textFgColor = fgColor
            textParams.textBgColor = bgColor
        }
        textParams.computedPosition.set(posX, posY)
        textParams.computedDimensions.set(textWidth, textHeight)
        textMatrix.get(textParams.quadMatrixArr)

        // Render text
        textRenderer.render(screenDimensions, textParams)
    }

    private class MutableTextParams(
        override var font: GameFont,
        override var fontScale: Float,
        override var transparentBg: Boolean,
        override var string: String,
        override var textAlign: UiTextElement.TextAlign,
        override var textFgColor: Int,
        override var textBgColor: Int
    ) : UiTextParams {
        override val computedDimensions: Vector2f = Vector2f()
        override val computedPosition: Vector2f = Vector2f()
        override val quadMatrixArr: FloatArray = FloatArray(16)
    }
}

interface UiTextAreaParams : UiHasTextParams, UiBoxParams {
    val lines: List<String>
    val lineSpacing: Float
    val lineFgColors: IntArray
    val lineBgColors: IntArray
}
