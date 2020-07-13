package drones

import org.joml.Matrix4f
import org.joml.Matrix4fc
import org.joml.Vector2f
import org.joml.Vector2fc
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFW.*
import java.lang.Float.min
import kotlin.math.sign
import kotlin.math.sqrt

class Camera {
    private val position = Vector2f()
    private val velocity = Vector2f()
    private val shiftMultiplier = 2.0f
    private val baseVelocity = 4f
    private val maxVelocity = baseVelocity * shiftMultiplier
    private val maxSqVelocity = maxVelocity * maxVelocity
    private val maxAccel = baseVelocity * 5f

    val positionc: Vector2fc = position

    private val matrix = Matrix4f()
    private val matrixInv = Matrix4f()
    val matrixArr = FloatArray(16)

    val matrixc: Matrix4fc = matrix
    val matrixInvc: Matrix4fc = matrixInv

    fun update(window: Long, deltaTime: Float) {
        // Update camera movement
        var inputX = 0f
        var inputY = 0f
        if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) inputX--
        if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) inputX++
        if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) inputY--
        if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) inputY++
        if (glfwGetKey(window, GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS) {
            inputX *= shiftMultiplier
            inputY *= shiftMultiplier
        }

        var desiredVelX = inputX * maxVelocity / shiftMultiplier
        var desiredVelY = inputY * maxVelocity / shiftMultiplier
        val desiredSqSpeed = desiredVelX * desiredVelX + desiredVelY * desiredVelY
        if (desiredSqSpeed > maxSqVelocity) {
            val desiredSpeed = sqrt(desiredSqSpeed)
            desiredVelX *= maxVelocity / desiredSpeed
            desiredVelY *= maxVelocity / desiredSpeed
        }

        velocity.x += sign(desiredVelX - velocity.x) * min(maxAccel * deltaTime, Math.abs(desiredVelX - velocity.x))
        velocity.y += sign(desiredVelY - velocity.y) * min(maxAccel * deltaTime, Math.abs(desiredVelY - velocity.y))

        val fac = (initialTileSize / tileSize) * deltaTime
        position.add(velocity.x * fac, velocity.y * fac)
    }

    fun updateMatrices(windowWidth: Int, windowHeight: Int) {
        matrix.setOrtho(position.x - windowWidth / tileSize / 2f,
            position.x + windowWidth / tileSize / 2f,
            position.y - windowHeight / tileSize / 2f,
            position.y + windowHeight / tileSize / 2f,
            -1f, 1f)
        matrix.invert(matrixInv)
        matrix.get(matrixArr)
    }
}
