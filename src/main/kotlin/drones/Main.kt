package drones

import org.joml.Matrix4f
import org.joml.Vector2f
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30.*
import org.lwjgl.opengl.GL43.GL_SHADER_STORAGE_BUFFER
import org.lwjgl.stb.STBImage
import org.lwjgl.system.MemoryUtil.*
import java.io.*
import java.lang.Float.min
import java.nio.ByteBuffer
import kotlin.math.sign
import kotlin.math.sqrt

class Main

var tileSize: Float = 64f

fun main(args: Array<String>) {
    val windowWidth = 1280
    val windowHeight = 720

    println("Hello world")

    // Set up OpenGL
    glfwInit()
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4)
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3)
    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)
    glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE)

    val window: Long = glfwCreateWindow(windowWidth, windowHeight, "Hello World", NULL, NULL)
    if (window == NULL) {
        println("Failed to create window");
        glfwTerminate()
        return
    }

    glfwSetKeyCallback(window, ::keyCallback)

    // Create the window
    glfwMakeContextCurrent(window)
    GL.createCapabilities()
    glfwSwapInterval(1)
    glfwShowWindow(window)

    // Vertices for a quad
    val quadVertices: FloatArray = floatArrayOf(
        -1f, -1f, 0.0f,
        1f, -1f, 0.0f,
        1f, 1f, 0.0f,

        -1f, 1f, 0f,
        -1f, -1f, 0f,
        1f, 1f, 0f
    )
    val quad2Vertices: FloatArray = floatArrayOf(
        -0.5f, -0.5f, 0.0f,     0f, 1f,
        0.5f, -0.5f, 0.0f,      1f, 1f,
        0.5f, 0.5f, 0.0f,       1f, 0f,

        -0.5f, 0.5f, 0f,        0f, 0f,
        -0.5f, -0.5f, 0f,       0f, 1f,
        0.5f, 0.5f, 0f,         1f, 0f
    )

    // Create Vertex Array Object (calls below will also bind things into this object)
    val vaoGrid = glGenVertexArrays()
    glBindVertexArray(vaoGrid)

    // Create vertex buffer object
    val vboGrid = glGenBuffers()
    glBindBuffer(GL_ARRAY_BUFFER, vboGrid)
    glBufferData(GL_ARRAY_BUFFER, quadVertices, GL_STATIC_DRAW)

    // Tell OpenGL how to use the vertex attributes for use by the future vertex shader
    glVertexAttribPointer(0, 3, GL_FLOAT, false, 12, 0) // set up vertex position for use in vertex shader
    glEnableVertexAttribArray(0)



    val vaoDrone = glGenVertexArrays()
    glBindVertexArray(vaoDrone)

    val vboDrone = glGenBuffers()
    glBindBuffer(GL_ARRAY_BUFFER, vboDrone)
    glBufferData(GL_ARRAY_BUFFER, quad2Vertices, GL_STATIC_DRAW)
    glVertexAttribPointer(0, 3, GL_FLOAT, false, 20, 0)
    glVertexAttribPointer(1, 2, GL_FLOAT, false, 20, 12)
    glEnableVertexAttribArray(0)
    glEnableVertexAttribArray(1)

    // Set up the shader
    val defaultVertexShader = Shader.create("/glsl/default.vert", GL_VERTEX_SHADER)
    val gridFragmentShader = Shader.create("/glsl/grid.frag", GL_FRAGMENT_SHADER)

    val objectVertexShader = Shader.create("/glsl/object.vert", GL_VERTEX_SHADER)
    val droneFragmentShader = Shader.create("/glsl/drone.frag", GL_FRAGMENT_SHADER)

    val gridShaderProgram = glCreateProgram()
    glAttachShader(gridShaderProgram, defaultVertexShader.glShaderObject)
    glAttachShader(gridShaderProgram, gridFragmentShader.glShaderObject)
    glLinkProgram(gridShaderProgram)

    val droneShaderProgram = Shader.createProgram(objectVertexShader, droneFragmentShader)

    // Set up bitmap (font) texture
    val font = loadFont()

    val bitmapTexture = glGenTextures()
    glBindTexture(GL_TEXTURE_2D, bitmapTexture)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, font.bitmapWidth, font.bitmapHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, font.bitmapTexture)
    glGenerateMipmap(GL_TEXTURE_2D)

    // Set up alpha blending
    glEnable(GL_BLEND)
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

    // Campbell color theme -- https://github.com/microsoft/terminal/releases/tag/1904.29002
    val colors = intArrayOf(
        0x0C0C0C,
        0x0037DA,
        0x13A10E,
        0x3A96DD,
        0xC50F1F,
        0x881798,
        0xC19C00,
        0xCCCCCC,
        0x767676,
        0x3B78FF,
        0x16C60C,
        0x61D6D6,
        0xE74856,
        0xB4009E,
        0xF9F1A5,
        0xF2F2F2
    )

    val ssbo = glGenBuffers()
    glBindBuffer(GL_SHADER_STORAGE_BUFFER, ssbo)
    glBufferData(GL_SHADER_STORAGE_BUFFER, 4096, GL_DYNAMIC_DRAW)
    glBufferSubData(GL_SHADER_STORAGE_BUFFER, 0, font.characterCoordinatesLut)
    glBufferSubData(GL_SHADER_STORAGE_BUFFER, 512, font.characterOffsetLut)
    glBufferSubData(GL_SHADER_STORAGE_BUFFER, 1024, floatArrayOf(font.bitmapWidth.toFloat(), font.bitmapHeight.toFloat()))
    glBufferSubData(GL_SHADER_STORAGE_BUFFER, 1032, colors)
    glBufferSubData(GL_SHADER_STORAGE_BUFFER, 1096, stringToBitmapArray("xxx@@a..B\\~{}%$", font))
    glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, ssbo)
    glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0)

    val startTime = System.currentTimeMillis()

    val grid = Grid(8, 8)
    grid.tiles[3][3] = TileStone
    grid.tiles[3][4] = TileStone
    grid.tiles[4][3] = TileStone
    grid.tiles[4][4] = TileStone

    val drone = Drone(Vector2f(0f, 0f), 0xEE9999, Vector2f(1f, 0f))

    var cameraX: Float = 0f
    var cameraY: Float = 0f
    var cameraVelX: Float = 0f
    var cameraVelY: Float = 0f
    val cameraMaxVel = 5f
    val cameraMaxSqVel = cameraMaxVel * cameraMaxVel
    val cameraAccel = cameraMaxVel * 5f

    var lastTime = System.currentTimeMillis()

    val cameraMatrixArr = FloatArray(16)

    // Loop
    while (!glfwWindowShouldClose(window)) {
        val deltaTime = (System.currentTimeMillis() - lastTime) * 0.001f
        lastTime = System.currentTimeMillis()

        // Update camera movement
        var inputX = 0
        var inputY = 0
        if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) inputX--
        if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) inputX++
        if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) inputY--
        if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) inputY++

        var desiredVelX = inputX * cameraMaxVel
        var desiredVelY = inputY * cameraMaxVel
        val desiredSqSpeed = desiredVelX * desiredVelX + desiredVelY * desiredVelY
        if (desiredSqSpeed > cameraMaxSqVel) {
            val desiredSpeed = sqrt(desiredSqSpeed)
            desiredVelX *= cameraMaxVel / desiredSpeed
            desiredVelY *= cameraMaxVel / desiredSpeed
        }

        cameraVelX += sign(desiredVelX - cameraVelX) * min(cameraAccel * deltaTime, Math.abs(desiredVelX - cameraVelX))
        cameraVelY += sign(desiredVelY - cameraVelY) * min(cameraAccel * deltaTime, Math.abs(desiredVelY - cameraVelY))

        cameraX += cameraVelX * deltaTime
        cameraY += cameraVelY * deltaTime

        // Update drone
        drone.position.add(drone.velocity.x * deltaTime, drone.velocity.y * deltaTime)
        drone.recomputeModelMatrix()

        // Render
        glClearColor(0f, 1f, 0f, 1f)
        glClear(GL_COLOR_BUFFER_BIT)

        // Render grid
        glUseProgram(gridShaderProgram)
        glUniform2f(glGetUniformLocation(gridShaderProgram, "WindowSize"), windowWidth.toFloat(), windowHeight.toFloat())
        glUniform1f(glGetUniformLocation(gridShaderProgram, "TileSize"), tileSize)
        glUniform2f(glGetUniformLocation(gridShaderProgram, "CameraPos"), cameraX, cameraY)
        glUniform2f(glGetUniformLocation(gridShaderProgram, "GridTopLeft"),
            grid.positionTopLeft.x(), grid.positionTopLeft.y())

        val renderedGrid = grid.toBitmapArray(font)
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, ssbo)
        glBufferSubData(GL_SHADER_STORAGE_BUFFER, 1096, intArrayOf(grid.width))
        glBufferSubData(GL_SHADER_STORAGE_BUFFER, 1100, intArrayOf(renderedGrid.size))
        glBufferSubData(GL_SHADER_STORAGE_BUFFER, 1104, renderedGrid)
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0)

        glBindTexture(GL_TEXTURE_2D, bitmapTexture)
        glBindVertexArray(vaoGrid)
        glDrawArrays(GL_TRIANGLES, 0, 6)

        // Render drone
        val cameraMatrix = Matrix4f().ortho(cameraX - windowWidth / tileSize / 2f,
            cameraX + windowWidth / tileSize / 2f,
            cameraY - windowHeight / tileSize / 2f,
            cameraY + windowHeight / tileSize / 2f,
            -1f, 1f)
        cameraMatrix.get(cameraMatrixArr)

        glUseProgram(droneShaderProgram)
        glUniformMatrix4fv(glGetUniformLocation(droneShaderProgram, "cameraMatrix"), false, cameraMatrixArr)
        glUniformMatrix4fv(glGetUniformLocation(droneShaderProgram, "modelMatrix"), false, drone.modelMatrixArr)
        glUniform1i(glGetUniformLocation(droneShaderProgram, "packedCharacterUv"),
            font.characterCoordinatesLut[font.characterCodeLut['>']!!])
        glUniform2f(glGetUniformLocation(droneShaderProgram, "bitmapDimensions"),
            font.bitmapWidth.toFloat(), font.bitmapHeight.toFloat())
        glUniform1i(glGetUniformLocation(droneShaderProgram, "droneColor"), drone.color)

        glBindTexture(GL_TEXTURE_2D, bitmapTexture)
        glBindVertexArray(vaoDrone)
        glDrawArrays(GL_TRIANGLES, 0, 6)

        glfwPollEvents()
        glfwSwapBuffers(window)
    }

    // End
    glfwTerminate()
}

fun keyCallback(window: Long, key: Int, scancode: Int, actions: Int, mods: Int) {
    if (key == GLFW_KEY_ESCAPE) {
        glfwSetWindowShouldClose(window, true)
    }
    if (key == GLFW_KEY_EQUAL && actions == GLFW_PRESS) {
        tileSize += 8
    }
    if (key == GLFW_KEY_MINUS && actions == GLFW_PRESS) {
        tileSize -= 8
    }
}

fun readImage(filename: String): Triple<Int, Int, ByteBuffer> {
    val x = IntArray(1)
    val y = IntArray(1)
    val channels = IntArray(1)
    val bytes = STBImage.stbi_load(filename, x, y, channels, 4) ?:
        throw FileNotFoundException("Could not find file '$filename'")

    return Triple(x[0], y[0], bytes)
}

/**
 * Prepares a string for use by the shader
 */
fun stringToBitmapArray(string: String, font: GameFont): IntArray {
    // http://forum.lwjgl.org/index.php?topic=6546.0
    val arr = IntArray(string.length + 1)
    arr[0] = string.length

    for (idx in string.indices)
        arr[idx + 1] = font.characterCodeLut[string[idx]] ?: error("Font does not support character '${string[idx]}'")

    for (idx in 1..arr.lastIndex)
        arr[idx] = arr[idx] or (14 shl 16) or (1 shl 8)

    return arr
}
