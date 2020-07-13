package drones.render

import drones.game.LaserBeam
import org.lwjgl.opengl.GL20.glUniform4f
import org.lwjgl.opengl.GL43.glGetUniformLocation
import org.lwjgl.opengl.GL43.glUniform2f

class LaserBeamRenderer(private val laserBeam: LaserBeam, shaderProgram: Int) :
        GameObjectRenderer(laserBeam, shaderProgram) {
    private val locationLaserDimensions: Int
    private val locationLaserColor: Int

    init {
        locationLaserDimensions = glGetUniformLocation(shaderProgram, "LaserDimensions")
        locationLaserColor = glGetUniformLocation(shaderProgram, "LaserColor")
    }

    override fun setUniforms() {
        glUniform2f(locationLaserDimensions, laserBeam.actualLength, laserBeam.width)
        glUniform4f(locationLaserColor, laserBeam.colorR, laserBeam.colorG, laserBeam.colorB, 1.0f)
    }
}
