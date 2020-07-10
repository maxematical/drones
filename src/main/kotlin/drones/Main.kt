package drones

import drones.scripting.*
import org.dyn4j.dynamics.Body
import org.dyn4j.dynamics.BodyFixture
import org.dyn4j.dynamics.RaycastResult
import org.dyn4j.dynamics.World
import org.dyn4j.geometry.Circle
import org.dyn4j.geometry.Mass
import org.dyn4j.geometry.Rectangle
import org.dyn4j.geometry.Vector2
import org.joml.Vector2f
import org.joml.Vector2i
import org.joml.Vector2ic
import org.joml.Vector4f
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL30.*
import org.lwjgl.opengl.GL43.GL_SHADER_STORAGE_BUFFER
import org.lwjgl.stb.STBImage
import org.lwjgl.system.MemoryUtil.NULL
import java.io.FileNotFoundException
import java.nio.ByteBuffer
import kotlin.math.floor

class Main

val initialTileSize: Float = 64f
var tileSize: Float = initialTileSize

var paused: Boolean = false

// Set by mouseCallback when mouse buttons pressed, in the main loop if this is true, will handle a mouse click
var mouseLeftClicked: Boolean = false
var mouseRightClicked: Boolean = false

fun main(args: Array<String>) {
    val windowWidth = 1280
    val windowHeight = 720

    println("Starting game")

    // Set up OpenGL
    glfwInit()
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4)
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3)
    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)
    glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE)
    glfwWindowHint(GLFW_DOUBLEBUFFER, GLFW_TRUE) // Whether to enable vsync. If false, call Flush instead of SwapBuffers

    val window: Long = glfwCreateWindow(windowWidth, windowHeight, "Hello World", NULL, NULL)
    if (window == NULL) {
        println("Failed to create window");
        glfwTerminate()
        return
    }

    glfwSetKeyCallback(window, ::keyCallback)
    glfwSetMouseButtonCallback(window, ::mouseCallback)

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

    val vaoFps = glGenVertexArrays()
    glBindVertexArray(vaoFps)
    val vboFps = glGenBuffers()
    glBindBuffer(GL_ARRAY_BUFFER, vboFps)
    glBufferData(GL_ARRAY_BUFFER, quadVertices, GL_STATIC_DRAW)
    glVertexAttribPointer(0, 3, GL_FLOAT, false, 12, 0)
    glEnableVertexAttribArray(0)

    // Set up the shader
    val defaultVertexShader = Shader.create("/glsl/default.vert", GL_VERTEX_SHADER)
    val gridFragmentShader = Shader.create("/glsl/grid.frag", GL_FRAGMENT_SHADER)

    val objectVertexShader = Shader.create("/glsl/object.vert", GL_VERTEX_SHADER)
    val droneFragmentShader = Shader.create("/glsl/drone.frag", GL_FRAGMENT_SHADER)

    val uiVertexShader = Shader.create("/glsl/ui.vert", GL_VERTEX_SHADER)
    val fpsFragmentShader = Shader.create("/glsl/fpscount.frag", GL_FRAGMENT_SHADER)

    val laserFsh = Shader.create("/glsl/laserbeam.frag", GL_FRAGMENT_SHADER)

    val gridShaderProgram = Shader.createProgram(defaultVertexShader, gridFragmentShader)
    val droneShaderProgram = Shader.createProgram(objectVertexShader, droneFragmentShader)
    val fpsShaderProgram = Shader.createProgram(uiVertexShader, fpsFragmentShader)
    val laserShaderProgram = Shader.createProgram(objectVertexShader, laserFsh)

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

    // Setup game
    // Setup camera
    val camera = Camera()

    // Setup grid
    val grid = Grid(24, 24)
    grid.tiles[3][3] = TileStone
    grid.tiles[3][4] = TileStone
    grid.tiles[4][3] = TileStone
    grid.tiles[4][4] = TileStone

    // Setup drone
    val drone = Drone(grid, Vector2f(-7f, 3f), 0xEEEEEE)
    drone.renderer = DroneRenderer(drone, droneShaderProgram, font,  bitmapTexture)

    val scriptMgr = ScriptManager(drone, "drone_manual_miner.lua", Int.MAX_VALUE) { globals ->
        ModuleVector.install(globals)
        ModuleCore(drone).install(globals)
        ModuleScanner(drone, this).install(globals)
        ModuleMiningLaser(drone).install(globals)
        globals.set("move", globals.loadfile("libmove.lua").call())
        globals.loadfile("libscanner.lua").call()
    }
    scriptMgr.onComplete = {
        drone.desiredVelocity.set(0f, 0f)
    }

    // Setup physics
    val world = World()
    world.gravity.set(0.0, 0.0)

    val gridBody = Body(4)
    for (y in 0 until grid.height) {
        for (x in 0 until grid.width) {
            if (grid.tiles[y][x] != TileAir) {
                val rectangle = Rectangle(1.0, 1.0)
                rectangle.translate(grid.gridToWorldX(x).toDouble() + 0.5, grid.gridToWorldY(y).toDouble() - 0.5)
                val fixture = BodyFixture(rectangle)
                fixture.userData = Vector2i(x, y)
                gridBody.addFixture(fixture)
            }
        }
    }
    world.addBody(gridBody)

    val droneBody = Body(1)
    droneBody.addFixture(Circle(drone.size.toDouble() * 0.5))
    droneBody.mass = Mass(Vector2(0.5, 0.5), 1.0, 1.0)
    droneBody.transform.setTranslation(drone.position.x.toDouble(), drone.position.y.toDouble())
    world.addBody(droneBody)

    // Init Fps counter
    var lastFps: Int = 0
    var fpsCountStart = System.currentTimeMillis()
    var fpsFramesCount = 0

    // Misc.
    val mouseXArr = DoubleArray(1)
    val mouseYArr = DoubleArray(1)
    val selectedDrones = mutableListOf<Drone>()
    var lastTime = System.currentTimeMillis()

    // Loop
    while (!glfwWindowShouldClose(window)) {
        val deltaTime = (System.currentTimeMillis() - lastTime) * 0.001f
        lastTime = System.currentTimeMillis()

        // Update camera
        camera.update(window, deltaTime)
        camera.updateMatrices(windowWidth, windowHeight)

        // Update game world
        if (!paused) {
            // Update drone
            val accel: Vector2f = Vector2f(drone.desiredVelocity).sub(droneBody.linearVelocity.toJoml())
            if (accel.lengthSquared() > 0)
                accel.normalize().mul(20f * deltaTime)

            droneBody.applyForce(accel.toDyn4j())
            drone.position.set(droneBody.transform.translation.x, droneBody.transform.translation.y)

            if (droneBody.linearVelocity.magnitudeSquared > 1) {
                droneBody.linearVelocity.normalize()
            }

            val desiredRotation: Float
            if (drone.desiredVelocity.lengthSquared() > 0.0625f) {
                desiredRotation = MathUtils.RAD2DEG *
                        Math.atan2(drone.desiredVelocity.y.toDouble(), drone.desiredVelocity.x.toDouble()).toFloat()
            } else {
                desiredRotation = drone.rotation
            }
            val deltaRotation = MathUtils.clampRotation(desiredRotation - drone.rotation)

            val desiredRotationSpeed = 150f * Math.min(1f, Math.abs(deltaRotation) / 60f)
            val changeRotation = Math.signum(deltaRotation) *
                    Math.min(Math.abs(deltaRotation), deltaTime * desiredRotationSpeed)
            drone.rotation += changeRotation

            drone.localTime += deltaTime

            drone.recomputeModelMatrix()

            // Update script
            scriptMgr.update()

            // Update laser beam
            drone.laserBeam?.let { laser ->
                laser.lifetime += deltaTime

                val rotationRad = (laser.rotation * MathUtils.DEG2RAD).toDouble()
                val laserStart = laser.position.toDyn4j()
                val laserEnd = laserStart.copy().add(Math.cos(rotationRad) * laser.unobstructedLength,
                    Math.sin(rotationRad) * laser.unobstructedLength)

                val raycastResult = mutableListOf<RaycastResult>()
                if (world.raycast(laserStart, laserEnd, { true }, true, false, false, raycastResult)) {
                    laser.actualLength = raycastResult[0].raycast.distance.toFloat()
                } else {
                    laser.actualLength = laser.unobstructedLength
                }

                if (laser.lifetime >= 2.0f) {
                    laser.lifetime = 0f

                    if (raycastResult.isNotEmpty()) {
                        val hit = raycastResult[0]
                        if (hit.body == gridBody) {
                            val hitGridCoordinates = hit.fixture.userData as Vector2ic
                            grid.tiles[hitGridCoordinates.y()][hitGridCoordinates.x()] = TileAir
                            hit.body.removeFixture(hit.fixture)
                        }
                    }
                }
            }

            // Update physics
            world.update(deltaTime.toDouble())
        }

        // Handle mouse click
        if (mouseLeftClicked || mouseRightClicked) {
            // (0,0) means the top left corner of the window. Mouse position is measured in pixels.
            glfwGetCursorPos(window, mouseXArr, mouseYArr)

            // Transform mouse coordinates to [-1,1] range
            val mouseX = -1f + 2f * (mouseXArr[0].toFloat() / windowWidth)
            val mouseY = 1f - 2f * (mouseYArr[0].toFloat() / windowHeight)

            // Begin transforming the mouse position to desired spaces
            val transformedMousePos = Vector4f(mouseX, mouseY, 0f, 1f)
            camera.matrixInvc.transform(transformedMousePos) // now is in world space

            if (mouseLeftClicked) {
                // Check if we're clicking on the drone
                drone.modelMatrix.invert(drone.modelMatrixInv)

                drone.modelMatrixInv.transform(transformedMousePos)

                // We now have the mouse coordinates relative to the drone's quad
                val clickedOnDrone = Math.abs(transformedMousePos.x) <= 0.5 && Math.abs(transformedMousePos.y) <= 0.5
                if (clickedOnDrone) {
                    if (drone in selectedDrones) {
                        println("Drone deselect")
                        selectedDrones.remove(drone)
                    } else {
                        println("Drone select")
                        selectedDrones.add(drone)
                    }
                }
            }
            if (mouseRightClicked) {
                for (selectedDrone in selectedDrones) {
                    selectedDrone.destination.set(transformedMousePos.x, transformedMousePos.y)
                    selectedDrone.destinationTargetDistance = 0.5f
                    selectedDrone.hasDestination = true
                }
            }

            mouseLeftClicked = false
            mouseRightClicked = false
        }

        // Update framerate counter
        fpsFramesCount++
        val fpsTime = System.currentTimeMillis() - fpsCountStart
        if (fpsTime >= 500) {
            lastFps = floor(fpsFramesCount * 1000f / fpsTime).toInt()
            fpsFramesCount = 0
            fpsCountStart = System.currentTimeMillis()
        }

        // Render
        glClearColor(0f, 1f, 0f, 1f)
        glClear(GL_COLOR_BUFFER_BIT)

        // Render grid
        glUseProgram(gridShaderProgram)
        glUniform2f(glGetUniformLocation(gridShaderProgram, "WindowSize"), windowWidth.toFloat(), windowHeight.toFloat())
        glUniform1f(glGetUniformLocation(gridShaderProgram, "TileSize"), tileSize)
        glUniform2f(glGetUniformLocation(gridShaderProgram, "CameraPos"), camera.positionc.x(), camera.positionc.y())
        glUniform2f(glGetUniformLocation(gridShaderProgram, "GridTopLeft"),
            grid.positionTopLeft.x(), -grid.positionTopLeft.y())

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
        drone.renderer?.render(camera.matrixArr)

        // Render laser beam
        drone.laserBeam?.let { laser ->
            if (laser.renderer == null) {
                laser.renderer = LaserBeamRenderer(laser, laserShaderProgram)
            }
            laser.renderer?.render(camera.matrixArr)
        }

        // Render fps counter
        glUseProgram(fpsShaderProgram)
        glUniform2f(glGetUniformLocation(fpsShaderProgram, "WindowSize"), windowWidth.toFloat(), windowHeight.toFloat())
        glUniform2f(glGetUniformLocation(fpsShaderProgram, "UiAnchorPoint"), 1f, 1f)
        // UiPositionPx: y=0 means bottom
        glUniform2f(glGetUniformLocation(fpsShaderProgram, "UiPositionPx"),
            windowWidth.toFloat() - 14f, windowHeight.toFloat() - 10f)
        glUniform2f(glGetUniformLocation(fpsShaderProgram, "UiDimensionsPx"), 120f, 38f)
        glUniform1f(glGetUniformLocation(fpsShaderProgram, "FontScale"), 2f)
        glUniform1f(glGetUniformLocation(fpsShaderProgram, "FontSpacing"), 12f)

        glBindBuffer(GL_SHADER_STORAGE_BUFFER, ssbo)
        glBufferSubData(GL_SHADER_STORAGE_BUFFER, 1096, stringToBitmapArray(lastFps.toString(), font, 0, 10))
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0)

        glBindVertexArray(vaoFps)
        glBindTexture(GL_TEXTURE_2D, bitmapTexture)
        glDrawArrays(GL_TRIANGLES, 0, 6)

        glfwPollEvents()
        glfwSwapBuffers(window)
//        glFlush()
    }

    // End
    glfwTerminate()

    // TODO: Figure out a way to terminate the LuaThread
    // https://stackoverflow.com/q/24585279
    // This would most likely either involve making custom implementation of coroutines that doesn't use threads --
    // preferably we could avoid threads anyways and without a way to shutdown LuaThreads, it's impossible to e.g.
    // remove a drone without having an extra thread floating around
    System.exit(0)
}

fun keyCallback(window: Long, key: Int, scancode: Int, action: Int, mods: Int) {
    if (key == GLFW_KEY_ESCAPE) {
        glfwSetWindowShouldClose(window, true)
    }
    if (key == GLFW_KEY_EQUAL && action == GLFW_PRESS) {
        tileSize += 8
    }
    if (key == GLFW_KEY_MINUS && action == GLFW_PRESS) {
        tileSize -= 8
    }
    if (key == GLFW_KEY_SPACE && action == GLFW_PRESS) {
        paused = !paused
    }
}

fun mouseCallback(window: Long, button: Int, action: Int, mods: Int) {
    if (button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_PRESS) {
        mouseLeftClicked = true
    }
    if (button == GLFW_MOUSE_BUTTON_RIGHT && action == GLFW_PRESS) {
        mouseRightClicked = true
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
fun stringToBitmapArray(string: String, font: GameFont, backgroundColor: Int = 1, foregroundColor: Int = 14): IntArray {
    // http://forum.lwjgl.org/index.php?topic=6546.0
    val arr = IntArray(string.length + 1)
    arr[0] = string.length

    for (idx in string.indices)
        arr[idx + 1] = font.characterCodeLut[string[idx]] ?: error("Font does not support character '${string[idx]}'")

    for (idx in 1..arr.lastIndex)
        arr[idx] = arr[idx] or (foregroundColor shl 16) or (backgroundColor shl 8)

    return arr
}
