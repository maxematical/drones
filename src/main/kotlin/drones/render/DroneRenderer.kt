package drones.render

import drones.game.Drone
import drones.GameFont
import org.lwjgl.opengl.GL30.*

class DroneRenderer(private val drone: Drone, shaderProgram: Int,
                    private val font: GameFont
) : GameObjectRenderer(drone, shaderProgram) {
    private val locationPackedCharacterUv: Int
    private val locationBitmapDimensions: Int
    private val locationDroneColor: Int
    private val locationLedColor: Int
    private val locationIsSelected: Int

    init {
        locationPackedCharacterUv = glGetUniformLocation(shaderProgram, "packedCharacterUv")
        locationBitmapDimensions = glGetUniformLocation(shaderProgram, "bitmapDimensions")
        locationDroneColor = glGetUniformLocation(shaderProgram, "droneColor")
        locationLedColor = glGetUniformLocation(shaderProgram, "ledColor")
        locationIsSelected = glGetUniformLocation(shaderProgram, "isSelected")
    }

    override fun setUniforms() {
        val packed = font.characterCoordinatesLut[font.characterCodeLut.getValue('>')]
        val uvWidth = (packed shr 7) and 127
        val uvHeight = packed and 127
        val packedCharacterUv = (packed and (16383.inv())) or ((uvWidth - 4) shl 7) or (uvHeight - 4)

        glUniform1i(locationPackedCharacterUv, packedCharacterUv)
        glUniform2f(locationBitmapDimensions, font.bitmapWidth.toFloat(), font.bitmapHeight.toFloat())
        glUniform1i(locationDroneColor, drone.color)
        glUniform1i(locationLedColor, drone.ledColor)
        glUniform1i(locationIsSelected, if (drone.selected) GL_TRUE else GL_FALSE)
        glBindTexture(GL_TEXTURE_2D, font.glBitmap)
    }
}
