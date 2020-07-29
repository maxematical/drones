package drones.render

import drones.game.Drone
import drones.loadTexture
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30.*

class DroneRenderer(private val drone: Drone, shaderProgram: Int) : GameObjectRenderer(drone, shaderProgram) {
    private val texture: Int
    private val textureWidth: Float
    private val textureHeight: Float

    private val locationTextureDimensions: Int
    private val locationDroneColor: Int
    private val locationLedColor: Int
    private val locationIsSelected: Int
    private val locationTime: Int

    init {
        // Load texture
        val (textureWidth, textureHeight, textureData) = loadTexture("/sprites/drone.png")

        texture = GL11.glGenTextures()
        glBindTexture(GL_TEXTURE_2D, texture)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, textureWidth, textureHeight,
            0, GL_RGBA, GL11.GL_UNSIGNED_BYTE, textureData)
        glGenerateMipmap(GL_TEXTURE_2D)

        this.textureWidth = textureWidth.toFloat()
        this.textureHeight = textureHeight.toFloat()

        // Load uniform locations
        locationTextureDimensions = glGetUniformLocation(shaderProgram, "TextureDimensions")
        locationDroneColor = glGetUniformLocation(shaderProgram, "DroneColor")
        locationLedColor = glGetUniformLocation(shaderProgram, "LedColor")
        locationIsSelected = glGetUniformLocation(shaderProgram, "IsSelected")
        locationTime = glGetUniformLocation(shaderProgram, "Time")
    }

    override fun setUniforms() {
        glUniform2f(locationTextureDimensions, textureWidth, textureHeight)
        glUniform1i(locationDroneColor, drone.color)
        glUniform1i(locationLedColor, drone.ledColor)
        glUniform1i(locationIsSelected, if (drone.selected) GL_TRUE else GL_FALSE)
        //glUniform1f(locationTime, drone.localTime)
        glBindTexture(GL_TEXTURE_2D, texture)
    }
}
