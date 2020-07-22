package drones.ui

interface GraphicsManager {
    val boxShaderProgram: Int
    val textShaderProgram: Int
    val ssbo: Int
}

interface UiGraphicsManager : GraphicsManager {
    val boxRenderer: UiBoxRenderer
    val textRenderer: UiTextRenderer
    val textAreaRenderer: UiTextAreaRenderer
}
