package drones

import drones.game.*
import drones.render.*
import drones.scripting.*
import drones.ui.*
import org.dyn4j.dynamics.Body
import org.dyn4j.dynamics.BodyFixture
import org.dyn4j.dynamics.World
import org.dyn4j.geometry.Rectangle
import org.joml.*
import org.luaj.vm2.Globals
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL30.*
import org.lwjgl.opengl.GL43.GL_SHADER_STORAGE_BUFFER
import org.lwjgl.stb.STBImage
import org.lwjgl.system.MemoryUtil.NULL
import org.slf4j.LoggerFactory
import java.io.FileNotFoundException
import java.nio.ByteBuffer
import java.util.*
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

    // Set up the shader
    val defaultVsh = Shader.create("/glsl/default.vert", GL_VERTEX_SHADER)
    val gridFsh = Shader.create("/glsl/grid.frag", GL_FRAGMENT_SHADER)

    val objectVsh = Shader.create("/glsl/object.vert", GL_VERTEX_SHADER)
    val droneFsh = Shader.create("/glsl/drone.frag", GL_FRAGMENT_SHADER)

    val uiVsh = Shader.create("/glsl/ui.vert", GL_VERTEX_SHADER)
    val uiTextFsh = Shader.create("/glsl/uitext.frag", GL_FRAGMENT_SHADER)
    val uiBoxFsh = Shader.create("/glsl/uibox.frag", GL_FRAGMENT_SHADER)

    val laserFsh = Shader.create("/glsl/laserbeam.frag", GL_FRAGMENT_SHADER)

    val simpleObjFsh = Shader.create("/glsl/simpleobject.frag", GL_FRAGMENT_SHADER)

    val debugDotFsh = Shader.create("/glsl/debugdot.frag", GL_FRAGMENT_SHADER)

    val gridShaderProgram = Shader.createProgram(defaultVsh, gridFsh)
    val droneShaderProgram = Shader.createProgram(objectVsh, droneFsh)
    val uiTextShaderProgram = Shader.createProgram(uiVsh, uiTextFsh)
    val uiBoxShaderProgram = Shader.createProgram(uiVsh, uiBoxFsh)
    val laserShaderProgram = Shader.createProgram(objectVsh, laserFsh)
    val simpleObjShaderProgram = Shader.createProgram(objectVsh, simpleObjFsh)
    val debugDotShaderProgram = Shader.createProgram(defaultVsh, debugDotFsh)

    // Set up bitmap (font) texture
    val font = loadFont()

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

    // Setup drones
    val baseLocation = Vector2f(3f, -3f)
    val drone1 = Drone(grid, Vector2f(-2f, 3f), 0xEEEEEE, 90f)
    val drone2 = Drone(grid, Vector2f(2f, 0f), 0x888888, 90f)
    drone1.scriptOrigin = baseLocation
    drone2.scriptOrigin = baseLocation

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

    val screenDimensions = Vector2f(windowWidth.toFloat(), windowHeight.toFloat())
    val windowContainer = WindowContainer(screenDimensions)

    // Init Fps counter
    val fpsCounter = UiText(Ui.Params(windowContainer,
        { dims, _ -> dims.set(120f, 38f) },
        { anchor, _ -> anchor.set(1f, 1f) },
        { pos, c -> pos.set(c.width - 14f, c.height - 10f) }))
    fpsCounter.textAlign = Ui.TextAlign.RIGHT
    fpsCounter.textFgColor = 10
    fpsCounter.renderer = UiTextRenderer(fpsCounter, uiTextShaderProgram, ssbo, font)

    var lastFps: Int = 0
    var fpsCountStart = System.currentTimeMillis()
    var fpsFramesCount = 0

    // Init paused text
    val pausedLabel = UiText(Ui.Params(windowContainer,
        { dims, _ -> dims.set(180f, 30f) },
        { anchor, _ -> anchor.set(0f, -1f) },
        { pos, c -> pos.set(c.width * 0.5f, 40f) }))
    pausedLabel.transparentTextBg = true
    pausedLabel.textAlign = Ui.TextAlign.CENTER
    pausedLabel.renderer = UiTextRenderer(pausedLabel, uiTextShaderProgram, ssbo, font)

    // Init info box
    val infoBox = UiBox(Ui.Params(windowContainer,
        { dims, _ -> dims.set(300f, 0f) },
        { anchor, _ -> anchor.set(1f, 0f) },
        { pos, c -> pos.set(c.width - 10f, c.height * 0.5f) },
        { pad, _ -> pad.set(0f, 10f, 100f, 10f) },
        allowOverflow = false))
    infoBox.renderer = UiBoxRenderer(infoBox, uiBoxShaderProgram)

    val infoBoxText = UiText(Ui.Params(infoBox,
        { dims, _ -> dims.set(150f, 30f) },
        { anchor, _ -> anchor.set(-1f, -1f) },
        { pos, c -> pos.set(0f, 0f) }))
    infoBoxText.requestedString = "In Box"
    infoBoxText.textBgColor = 12
    infoBoxText.renderer = UiTextRenderer(infoBoxText, uiTextShaderProgram, ssbo, font)

    /*

    UiGrid.rows(nRows = 2)
            .add(UiText("Hello:"), width = CellSize.Auto)
            .add(UiText("56%"), width = CellSize.Auto)

     */

    var debugDot: DebugDotRenderer? = null
    debugDot = DebugDotRenderer(debugDotShaderProgram, infoBox.bottomLeft)

    // Misc.
    val logger = LoggerFactory.getLogger(Main::class.java)
    val mouseXArr = DoubleArray(1)
    val mouseYArr = DoubleArray(1)
    val selectedDrones = mutableListOf<Drone>()
    var lastTime = System.currentTimeMillis()
    var gameTime = 0f

    val gameObjects = mutableListOf<GameObject>()

    val gameState = GameState(world, grid, gridBody, LinkedList(), LinkedList(), gameObjects)

    val installScripts: (Drone) -> ScriptManager.(Globals) -> Unit = { drone -> { globals ->
        ModuleVector.install(globals)
        ModuleCore(drone).install(globals)
        ModuleScanner(drone, this).install(globals)
        ModuleMiningLaser(drone).install(globals)
        ModuleTractorBeam(drone, gameState).install(globals)
        globals.set("move", globals.loadfile("libmove.lua").call())
        globals.loadfile("libscanner.lua").call()
    } }
    val scriptMgr1 = ScriptManager(drone1, "drone_ore_search.lua", Int.MAX_VALUE, installScripts(drone1))
    scriptMgr1.onComplete = { drone1.desiredVelocity.set(0f, 0f) }
    val scriptMgr2 = ScriptManager(drone2, "drone_manual_miner.lua", Int.MAX_VALUE, installScripts(drone2))
    scriptMgr2.onComplete = { drone2.desiredVelocity.set(0f, 0f) }

    // Spawn the objects
    val spawnObject: (GameObject) -> Unit = { gameObject ->
        logger.info("Spawning object $gameObject")
        if (gameObject.physicsBody != null) {
            world.addBody(gameObject.physicsBody)
        }
        gameObject.renderer = when (gameObject) {
            is Drone -> DroneRenderer(gameObject, droneShaderProgram, font)
            is LaserBeam -> LaserBeamRenderer(gameObject, laserShaderProgram)
            is Base -> SimpleObjectRenderer(gameObject, simpleObjShaderProgram, font, '#', 2f, true)
            is OreChunk -> SimpleObjectRenderer(gameObject, simpleObjShaderProgram, font, 'o')
            else -> throw RuntimeException("Could not create renderer for GameObject $gameObject")
        }
        gameObject.behavior = when (gameObject) {
            is Drone -> DroneBehavior(gameState, gameObject)
            is LaserBeam -> if (gameObject.behavior is IdleBehavior) LaserBeamBehavior(gameState, gameObject) else gameObject.behavior
            is Base -> gameObject.behavior
            is OreChunk -> gameObject.behavior
            else -> throw RuntimeException("Could not create behavior for GameObject $gameObject")
        }
        if (gameObject is Drone)
            gameObject.hoverable = DroneHoverable(gameObject)
        gameObjects.add(gameObject)
        gameObject.spawned = true
        gameObject.spawnedTime = gameTime
        logger.info("Object was successfully spawned")
    }
    val despawnObject: (GameObject) -> Unit = { gameObject ->
        logger.info("Despawning object $gameObject")
        gameObject.behavior.remove()

        if (gameObject.physicsBody != null) {
            world.removeBody(gameObject.physicsBody)
        }
        gameObjects.remove(gameObject)
        gameObject.spawned = false
        gameObject.requestDespawn = false
        logger.info("Object was successfully despawned")
    }

    spawnObject(Base(baseLocation))
    spawnObject(drone1)
    spawnObject(drone2)
    spawnObject(OreChunk(Vector2f(5f, 5f), 0f))

    // Loop
    while (!glfwWindowShouldClose(window)) {
        val deltaTime = (System.currentTimeMillis() - lastTime) * 0.001f
        lastTime = System.currentTimeMillis()

        // Update camera
        camera.update(window, deltaTime)
        camera.updateMatrices(windowWidth, windowHeight)

        // Update game world
        if (!paused) {
            gameTime += deltaTime

            // Update gameObjects
            for (gameObject in gameObjects) {
                gameObject.behavior.update(deltaTime)
                gameObject.recomputeModelMatrix()

                gameObject.physicsBody?.userData = gameObject
            }

            for (gameObject in gameObjects) {
                if (gameObject.requestDespawn && gameObject !in gameState.despawnQueue) {
                    gameState.despawnQueue.add(gameObject)
                }
            }

            // Perform spawning/despawning
            for (toSpawn in gameState.spawnQueue) {
                spawnObject(toSpawn)
            }
            for (toDespawn in gameState.despawnQueue) {
                despawnObject(toDespawn)
            }
            gameState.spawnQueue.clear()
            gameState.despawnQueue.clear()

            // Update scripts
            scriptMgr1.update(gameState)
            scriptMgr2.update(gameState)

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
                // Check if we're clicking on any drones
                var clickedOnDrone = false

                for (obj in gameObjects) {
                    if (obj is Drone && obj.hoverable.isHover(transformedMousePos)) {
                        clickedOnDrone = true
                        if (obj in selectedDrones) {
                            selectedDrones.remove(obj)
                            obj.selected = false
                        } else {
                            selectedDrones.add(obj)
                            obj.selected = true
                        }
                    }
                }

                // Deselect all drones if didn't click on anything
                if (!clickedOnDrone) {
                    for (drone in selectedDrones) {
                        drone.selected = false
                    }
                    selectedDrones.clear()
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
        glUniform1f(glGetUniformLocation(gridShaderProgram, "GridWidth"), grid.width.toFloat())

        val renderedGrid = grid.toBitmapArray(font)
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, ssbo)
        glBufferSubData(GL_SHADER_STORAGE_BUFFER, 1096, intArrayOf(renderedGrid.size))
        glBufferSubData(GL_SHADER_STORAGE_BUFFER, 1100, renderedGrid)
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0)

        glBindTexture(GL_TEXTURE_2D, font.glBitmap)
        glBindVertexArray(vaoGrid)
        glDrawArrays(GL_TRIANGLES, 0, 6)

        // Render game objects
        for (obj in gameObjects) {
            obj.renderer.render(screenDimensions, camera.matrixArr, gameTime)
        }

        // Render framerate counter
        fpsCounter.requestedString = lastFps.toString()
        fpsCounter.renderer?.render(screenDimensions, camera.matrixArr, gameTime)

        // Render paused reminder
        pausedLabel.requestedString = if (paused) "Paused" else ""
        pausedLabel.renderer?.render(screenDimensions, camera.matrixArr, gameTime)

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
