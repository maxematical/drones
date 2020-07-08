package drones

import org.lwjgl.opengl.GL30.*

class DroneRenderer(private val drone: Drone, private val shaderProgram: Int,
                    private val font: GameFont, private val bitmapTexture: Int) : Renderer {
    private val vao: Int

    private val locationCameraMatrix: Int
    private val locationModelMatrix: Int
    private val locationPackedCharacterUv: Int
    private val locationBitmapDimensions: Int
    private val locationDroneColor: Int
    private val locationLedColor: Int

    init {
        locationCameraMatrix = glGetUniformLocation(shaderProgram, "cameraMatrix")
        locationModelMatrix = glGetUniformLocation(shaderProgram, "modelMatrix")
        locationPackedCharacterUv = glGetUniformLocation(shaderProgram, "packedCharacterUv")
        locationBitmapDimensions = glGetUniformLocation(shaderProgram, "bitmapDimensions")
        locationDroneColor = glGetUniformLocation(shaderProgram, "droneColor")
        locationLedColor = glGetUniformLocation(shaderProgram, "ledColor")

        val vertices: FloatArray = floatArrayOf(
            -0.5f, -0.5f, 0.0f, 0f, 1f,
            0.5f, -0.5f, 0.0f, 1f, 1f,
            0.5f, 0.5f, 0.0f, 1f, 0f,

            -0.5f, 0.5f, 0f, 0f, 0f,
            -0.5f, -0.5f, 0f, 0f, 1f,
            0.5f, 0.5f, 0f, 1f, 0f
        )

        vao = glGenVertexArrays()
        glBindVertexArray(vao)

        val vbo = glGenBuffers()
        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 20, 0)
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 20, 12)
        glEnableVertexAttribArray(0)
        glEnableVertexAttribArray(1)
    }

    override fun render(cameraMatrixArr: FloatArray) {
        glUseProgram(shaderProgram)
        glUniformMatrix4fv(locationCameraMatrix, false, cameraMatrixArr)
        glUniformMatrix4fv(locationModelMatrix, false, drone.modelMatrixArr)
        glUniform1i(locationPackedCharacterUv, font.characterCoordinatesLut[font.characterCodeLut['>']!!])
        glUniform2f(locationBitmapDimensions, font.bitmapWidth.toFloat(), font.bitmapHeight.toFloat())
        glUniform1i(locationDroneColor, drone.color)
        glUniform1i(locationLedColor, drone.ledColor)

        glBindTexture(GL_TEXTURE_2D, bitmapTexture)
        glBindVertexArray(vao)
        glDrawArrays(GL_TRIANGLES, 0, 6)
    }
}
