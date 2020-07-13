package drones

import org.lwjgl.opengl.GL43.glGetUniformLocation
import org.lwjgl.opengl.GL43.glUniform2f

class LaserBeamRenderer(private val laserBeam: LaserBeam, shaderProgram: Int) :
        GameObjectRenderer(laserBeam, shaderProgram) {
    private val locationLaserDimensions: Int

    init {
        locationLaserDimensions = glGetUniformLocation(shaderProgram, "LaserDimensions")
    }

    override fun setUniforms() {
        glUniform2f(locationLaserDimensions, laserBeam.actualLength, laserBeam.width)
    }
}
