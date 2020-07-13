package drones.render

import drones.GameFont
import drones.game.Base
import org.lwjgl.opengl.GL30.*

class BaseRenderer(base: Base, shaderProgram: Int, private val font: GameFont) :
        GameObjectRenderer(base, shaderProgram) {
    private val locationPackedCharUv: Int
    private val locationBitmapDimensions: Int

    private val packedCharacterUv: Int

    init {
        locationPackedCharUv = glGetUniformLocation(shaderProgram, "PackedCharacterUv")
        locationBitmapDimensions = glGetUniformLocation(shaderProgram, "BitmapDimensions")
        packedCharacterUv = font.characterCoordinatesLut[font.characterCodeLut.getValue('#')]
    }

    override fun setUniforms() {
        glUniform1i(locationPackedCharUv, packedCharacterUv)
        glUniform2f(locationBitmapDimensions, font.bitmapWidth.toFloat(), font.bitmapHeight.toFloat())
        glBindTexture(GL_TEXTURE_2D, font.glBitmap)
    }
}
