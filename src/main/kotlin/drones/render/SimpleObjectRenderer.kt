package drones.render

import drones.GameFont
import drones.game.Base
import drones.game.GameObject
import org.lwjgl.opengl.GL30.*

class SimpleObjectRenderer(obj: GameObject, shaderProgram: Int, private val font: GameFont, private val character: Char,
                           private val numberPatterns: Float = 1f, private val switchPatterns: Boolean = false) :
        GameObjectRenderer(obj, shaderProgram) {
    private val locationPackedCharUv: Int
    private val locationBitmapDimensions: Int
    private val locationNumberPatterns: Int
    private val locationSwitchPatterns: Int

    private val packedCharacterUv: Int

    init {
        locationPackedCharUv = glGetUniformLocation(shaderProgram, "PackedCharacterUv")
        locationBitmapDimensions = glGetUniformLocation(shaderProgram, "BitmapDimensions")
        locationNumberPatterns = glGetUniformLocation(shaderProgram, "NumberPatterns")
        locationSwitchPatterns = glGetUniformLocation(shaderProgram, "SwitchPatterns")
        packedCharacterUv = font.characterCoordinatesLut[font.characterCodeLut.getValue(character)]
    }

    override fun setUniforms() {
        glUniform1i(locationPackedCharUv, packedCharacterUv)
        glUniform2f(locationBitmapDimensions, font.bitmapWidth.toFloat(), font.bitmapHeight.toFloat())
        glUniform1f(locationNumberPatterns, numberPatterns)
        glUniform1i(locationSwitchPatterns, if (switchPatterns) GL_TRUE else GL_FALSE)
        glBindTexture(GL_TEXTURE_2D, font.glBitmap)
    }
}
