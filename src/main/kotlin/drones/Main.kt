package drones

import drones.game.*
import drones.render.*
import drones.scripting.*
import drones.ui.*
import drones.ui.LayoutVector.Units.PERCENT
import drones.ui.LayoutVector.Units.PIXELS
import org.dyn4j.dynamics.World
import org.joml.Math
import org.joml.Vector2f
import org.joml.Vector2fc
import org.joml.Vector4f
import org.luaj.vm2.Globals
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL30.*
import org.lwjgl.opengl.GL43.GL_SHADER_STORAGE_BUFFER
import org.lwjgl.system.MemoryUtil.NULL
import org.slf4j.LoggerFactory
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

    val solidColorFsh = Shader.create("/glsl/solidcolor.frag", GL_FRAGMENT_SHADER)

    val gridShaderProgram = Shader.createProgram(defaultVsh, gridFsh)
    val droneShaderProgram = Shader.createProgram(objectVsh, droneFsh)
    val uiTextShaderProgram = Shader.createProgram(uiVsh, uiTextFsh)
    val uiBoxShaderProgram = Shader.createProgram(uiVsh, uiBoxFsh)
    val laserShaderProgram = Shader.createProgram(objectVsh, laserFsh)
    val simpleObjShaderProgram = Shader.createProgram(objectVsh, simpleObjFsh)
    val debugDotShaderProgram = Shader.createProgram(defaultVsh, debugDotFsh)

    // Set up bitmap (font) texture
    val font = loadFont("Lemon", "medium", 14)
    //val font = loadFont("Consolas", "regular", 14)

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
    // Store string data in buffer position 1096 -- first int should be the length of the string, then an array of size
    // the string length, where each int stores both the char code, background, and foreground color.
    // See UiTextRenderer.updateTextData for example
    glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, ssbo)
    glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0)

    // Setup game
    TileManager.registerTiles()

    // Setup camera
    val camera = Camera()

    // Setup grid
    val grid = Grid(24, 24)
    grid.setTile(3, 3, TileOre)
    grid.setTile(3, 4, TileOre)
    grid.setTile(4, 3, TileOre)
    grid.setTile(4, 4, TileOre)

    // Setup drones
    val baseLocation = Vector2f(3f, -3f)
    val drone1 = Drone(Vector2f(-2f, 3f), 0xEEEEEE, 90f)
    val drone2 = Drone(Vector2f(2f, 0f), 0x888888, 90f)
    drone1.scriptOrigin = baseLocation
    drone2.scriptOrigin = baseLocation
    drone1.createBehavior = CreateDroneBehavior(drone1)
    drone2.createBehavior = CreateDroneBehavior(drone2)

    // Setup physics
    val world = World()
    world.gravity.set(0.0, 0.0)

    world.addBody(grid.physicsBody)

    // Setup UI
    val screenDimensions = Vector2f(windowWidth.toFloat(), windowHeight.toFloat())

    val fpsCounter = UiTextElement(font, autoDimensions = LayoutVector(9f * 2f * 5f, 0f))
    fpsCounter.textFgColor = 10
    fpsCounter.fontScale = 2.0f
    fpsCounter.textAlign = UiTextElement.TextAlign.RIGHT_ALIGN
    fpsCounter.renderer = UiTextRenderer(fpsCounter, uiTextShaderProgram, ssbo)
    fpsCounter.rootComputeMeasurements(screenDimensions, screenDimensions, Vector2f(1f, 1f))

    var lastFps: Int = 0
    var fpsCountStart = System.currentTimeMillis()
    var fpsFramesCount = 0

    val sideBox = UiBoxElement(LayoutVector(240f, 240f))
    sideBox.setChild(UiVerticalLayout().apply {
        addChild(UiTextElement(font, "Hello").apply {
            renderer = UiTextRenderer(this, uiTextShaderProgram, ssbo)
            fontScale = 1.5f
            transparentBg = true
        })
        addChild(UiTextElement(font, "XX/XX HP").apply {
            renderer = UiTextRenderer(this, uiTextShaderProgram, ssbo)
            fontScale = 1.0f
            transparentBg = true
            textFgColor = 15
        })
        addChild(UiTextElement(font, "XX/XX Stored Power").apply {
            renderer = UiTextRenderer(this, uiTextShaderProgram, ssbo)
            fontScale = 1.0f
            transparentBg = true
            textFgColor = 15
        })
    })
    sideBox.centerChild = false
    sideBox.borderWidth = 3
    sideBox.padding.set(6f)
    sideBox.renderer = UiBoxRenderer(sideBox, uiBoxShaderProgram)
    sideBox.rootComputeMeasurements(screenDimensions,
        Vector2f(screenDimensions.x() - 8f, screenDimensions.y() * 0.5f), Vector2f(1f, 0.5f))
    var showBaseInfo = false
    var baseInfoTransition = 0f

    val tooltipBox = UiBoxElement()
    tooltipBox.padding.set(3f)
    tooltipBox.centerChild = false
    tooltipBox.borderWidth = 2
    tooltipBox.renderer = UiBoxRenderer(tooltipBox, uiBoxShaderProgram)
    val tooltipText = UiTextElement(font).apply {
        renderer = UiTextRenderer(this, uiTextShaderProgram, ssbo)
        lineSpacing = 1.0f
    }
    tooltipBox.setChild(tooltipText)
    val tooltipBoxPosition = Vector2f(0f, 0f) // Will be updated and used as an argument in rootUpdatePosition()

    val pausedText = UiTextElement(font, "Paused")
    pausedText.transparentBg = true
    pausedText.textAlign = UiTextElement.TextAlign.CENTER_ALIGN
    pausedText.fontScale = 2.0f
    pausedText.renderer = UiTextRenderer(pausedText, uiTextShaderProgram, ssbo)
    pausedText.rootComputeMeasurements(screenDimensions, Vector2f(screenDimensions.x() * 0.5f, 10f), Vector2f(0.5f, 0f))

    val droneInfoUi = DroneInfoUi(screenDimensions, font, ssbo, uiBoxShaderProgram, uiTextShaderProgram)

    var debugDot: DebugDotRenderer? = null
    //debugDot = DebugDotRenderer(debugDotShaderProgram, fpsCounter.computedPosition)

    // Misc.
    val logger = LoggerFactory.getLogger(Main::class.java)
    val mouseXArr = DoubleArray(1)
    val mouseYArr = DoubleArray(1)
    val selectedDrones = mutableListOf<Drone>()
    var lastTime = System.currentTimeMillis()
    var gameTime = 0f

    val gameObjects = mutableListOf<GameObject>()

    val gameState = GameState(world, grid, grid.physicsBody, LinkedList(), LinkedList(), gameObjects)

    val installScripts: (Drone) -> ScriptManager.(Globals) -> Unit = { drone -> { globals ->
        ModuleVector.install(globals)
        ModuleCore(drone).install(globals)
        ModuleScanner(drone).install(globals)
        ModuleMiningLaser(drone).install(globals)
        ModuleTractorBeam(drone, gameState).install(globals)
        globals.set("move", globals.loadfile("libmove.lua").call())
        globals.loadfile("libscanner.lua").call()
    } }
    val scriptMgr1 = ScriptManager("drone_ore_search.lua", Int.MAX_VALUE, installScripts(drone1))
    scriptMgr1.onComplete = { drone1.desiredVelocity.set(0f, 0f) }
    drone1.scriptManager = scriptMgr1
    val scriptMgr2 = ScriptManager("drone_manual_miner.lua", Int.MAX_VALUE, installScripts(drone2))
    scriptMgr2.onComplete = { drone2.desiredVelocity.set(0f, 0f) }
    drone2.scriptManager = scriptMgr2

    // Spawn the objects
    val spawnObject: (GameObject) -> Unit = { gameObject ->
        logger.info("Spawning object $gameObject")
        gameObject.renderer = when (gameObject) {
            is Drone -> DroneRenderer(gameObject, droneShaderProgram, font)
            is LaserBeam -> LaserBeamRenderer(gameObject, laserShaderProgram)
            is Base -> SimpleObjectRenderer(gameObject, simpleObjShaderProgram, font, '#', 2f, true)
            is OreChunk -> SimpleObjectRenderer(gameObject, simpleObjShaderProgram, font, 'o')
            else -> throw RuntimeException("Could not create renderer for GameObject $gameObject")
        }
        gameObject.hoverable = SimpleObjectHoverable(gameObject)
        gameObjects.add(gameObject)
        gameObject.spawned = true
        gameObject.spawnedTime = gameTime
        logger.info("Object was successfully spawned")
    }
    val despawnObject: (GameObject) -> Unit = { gameObject ->
        logger.info("Despawning object $gameObject")
        gameObject.behavior?.destroy()
        gameObject.physics?.destroy(gameState)

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
                if (gameObject.behavior == null) {
                    gameObject.behavior = gameObject.createBehavior.create(gameState)
                }
                gameObject.behavior?.update(deltaTime)

                gameObject.recomputeModelMatrix()
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

            // Update physics
            world.update(deltaTime.toDouble())
            for (gameObject in gameObjects)
                gameObject.physics?.update(gameState, deltaTime)
        }

        // Get mouse position
        // (0,0) means the top left corner of the window. Mouse position is measured in pixels.
        glfwGetCursorPos(window, mouseXArr, mouseYArr)
        val mouseX: Float = mouseXArr[0].toFloat()
        val mouseY: Float = mouseYArr[0].toFloat()

        // Transform mouse coordinates to [-1,1] range
        val normalizedX = -1f + 2f * (mouseX / windowWidth)
        val normalizedY = 1f - 2f * (mouseY / windowHeight)

        // Begin transforming the mouse position to desired spaces
        val transformedMousePos = Vector4f(normalizedX, normalizedY, 0f, 1f)
        camera.matrixInvc.transform(transformedMousePos) // now is in world space

        // Update tooltip
        var drawTooltip: Boolean = false
        for (obj in gameObjects) {
            if (obj.hoverable.isHover(transformedMousePos)) {
                drawTooltip = true

                var newText = obj.toString()
                if (obj is Drone)
                    newText = "Drone (${obj.inventory.currentVolume}/${obj.inventory.capacity}L)"
                if (tooltipText.string != newText) {
                    tooltipText.string = newText
                    tooltipBox.rootComputeMeasurements(screenDimensions)
                }
            }
        }

        // Handle mouse click
        if (mouseLeftClicked || mouseRightClicked) {
            if (mouseLeftClicked) {
                // Check if we're clicking on any drones or the base
                var clickedDrone = false
                var clickedBase = false

                for (obj in gameObjects) {
                    val hover = obj.hoverable.isHover(transformedMousePos)

                    if (obj is Drone && hover) {
                        clickedDrone = true

                        for (drone in selectedDrones)
                            drone.selected = false
                        selectedDrones.clear()

                        selectedDrones.add(obj)
                        obj.selected = true
                    }

                    if (obj is Base && hover) {
                        clickedBase = true
                    }
                }

                // Deselect all drones if didn't click on anything
                if (!clickedDrone) {
                    for (drone in selectedDrones) {
                        drone.selected = false
                    }
                    selectedDrones.clear()
                }

                // Only show base info if we clicked on it
                showBaseInfo = clickedBase
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

        // Render UI
        // Render FPS counter
        fpsCounter.string = lastFps.toString()
        fpsCounter.render(screenDimensions)

        // Render tooltip
        if (drawTooltip) {
            tooltipBoxPosition.set(mouseX, screenDimensions.y() - mouseY)
            tooltipBox.rootUpdatePosition(screenDimensions, tooltipBoxPosition)
            tooltipBox.render(screenDimensions)
        }

        // Render paused text
        if (paused) {
            pausedText.render(screenDimensions)
        }

        // Render base info box
        val desiredBaseInfoTransition: Float = if (showBaseInfo) 1f else 0f
        val prevTransition = baseInfoTransition
        baseInfoTransition = Math.clamp(0f, 1f,
            baseInfoTransition + java.lang.Math.signum(desiredBaseInfoTransition - 0.5f) * 6f * deltaTime)

        if (baseInfoTransition > 0) {
            if (baseInfoTransition != prevTransition) {
                val finalX = screenDimensions.x() - sideBox.computedDimensions.x() - 8f
                val sideBoxX = MathUtils.lerp(screenDimensions.x(), finalX, MathUtils.smoothstep(baseInfoTransition))

                sideBox.rootUpdatePosition(screenDimensions,
                    Vector2f(sideBoxX, screenDimensions.y() * 0.5f),
                    Vector2f(0f, 0.5f))
            }

            sideBox.render(screenDimensions)
        }

        // Render drone info
        if (selectedDrones.isNotEmpty())
            droneInfoUi.updateUi(selectedDrones[0])
        droneInfoUi.render(selectedDrones.isNotEmpty(), deltaTime)

        debugDot?.debugPosition = tooltipBox.computedPosition
        debugDot?.render(screenDimensions, camera.matrixArr, gameTime)

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

private class DroneInfoUi(private val screenDimensions: Vector2fc,
                          font: GameFont,
                          ssbo: Int,
                          boxShaderProgram: Int,
                          textShaderProgram: Int) {
    val box: UiBoxElement
    val droneName: UiTextElement
    val inventoryText: UiTextElement
    val inventoryContentsBox: UiBoxElement
    val inventoryContentsText1: UiTextElement

    val scriptInfoText: UiTextElement
    val scriptFunctionText: UiTextElement
    val scriptTextArea: UiTextArea

    private var transition: Float = 0.0f
    private val rootPosition = Vector2f()
    private val rootAnchor = Vector2f(0f, 0.5f)

    init {
        box = UiBoxElement(LayoutVector(240f, screenDimensions.y() - 100f))
        box.renderer = UiBoxRenderer(box, boxShaderProgram)
        box.backgroundColor = 1
        box.borderColor = 0x000000
        box.borderWidth = 5
        box.padding.set(5f)
        box.centerChild = false

        val vertical = UiVerticalLayout()
        box.setChild(vertical)

        droneName = UiTextElement(font, "Drone XYZ")
        droneName.renderer = UiTextRenderer(droneName, textShaderProgram, ssbo)
        //droneName.fontSize = 28
        droneName.fontScale = 2.0f
        droneName.transparentBg = true
        vertical.addChild(droneName)

        inventoryText = UiTextElement(font, "Inventory")
        inventoryText.renderer = UiTextRenderer(inventoryText, textShaderProgram, ssbo)
        //inventoryText.fontScale = 14
        inventoryText.transparentBg = false
        vertical.addChild(inventoryText)

        inventoryContentsBox = UiBoxElement(LayoutVector(100f, PERCENT, 0f, PERCENT))
        inventoryContentsBox.renderer = UiBoxRenderer(inventoryContentsBox, boxShaderProgram)
        inventoryContentsBox.borderWidth = 1
        inventoryContentsBox.backgroundColor = 0x000000
        vertical.addChild(inventoryContentsBox)

        inventoryContentsText1 = UiTextElement(font, "(Capacity goes here)")
        inventoryContentsText1.renderer = UiTextRenderer(inventoryContentsText1, textShaderProgram, ssbo)
        inventoryContentsText1.transparentBg = true
        inventoryContentsBox.setChild(inventoryContentsText1)

        vertical.addChild(UiTextElement(font, "Running Script:").apply {
            renderer = UiTextRenderer(this, textShaderProgram, ssbo)
            transparentBg = true
        })

        scriptInfoText = UiTextElement(font, autoDimensions = LayoutVector(100f, PERCENT, 0f, PERCENT))
        scriptInfoText.renderer = UiTextRenderer(scriptInfoText, textShaderProgram, ssbo)
        scriptInfoText.transparentBg = true
        vertical.addChild(scriptInfoText)

        scriptFunctionText = UiTextElement(font, autoDimensions = LayoutVector(100f, PERCENT, 0f, PERCENT)).apply {
            renderer = UiTextRenderer(this, textShaderProgram, ssbo)
            transparentBg = true
        }
        vertical.addChild(scriptFunctionText)

        scriptTextArea = UiTextArea(font, LayoutVector(100f, PERCENT, 0f, PERCENT), 6)
        scriptTextArea.string = "Hello World!!!\nWe can have\nMultiple lines!!\n  isnt that neat abcdefghijklmnop"
        scriptTextArea.renderer = UiTextAreaRenderer(scriptTextArea, ssbo, boxShaderProgram, textShaderProgram)
        vertical.addChild(scriptTextArea)

        box.rootComputeMeasurements(screenDimensions, Vector2f(screenDimensions.x(), screenDimensions.y() * 0.5f),
            Vector2f(1f, 0.5f))
    }

    fun updateUi(drone: Drone) {
        val l = drone.scriptManager?.currentLine

        inventoryContentsText1.string = "${drone.inventory.currentVolume}/${drone.inventory.capacity}L"
        scriptInfoText.string = l?.sourceFile ?: "(Not running script)"
        scriptFunctionText.string = l?.insideFunction?.let { "In $it()" } ?: ""

        val lines = drone.scriptManager?.luaSourceLines ?: emptyList()
        val startLine = l?.lineNumber ?: 3

        val concat = StringBuilder()
        for (lineIndex in (startLine - 3)..(startLine + 2)) {
            if (lineIndex in lines.indices) {
                var formattedLineNumber: String = (lineIndex + 1).toString()
                if (formattedLineNumber.length == 1)
                    formattedLineNumber = ' ' + formattedLineNumber

                concat.append(formattedLineNumber).append(' ').append(lines[lineIndex]).append('\n')
            }
        }

        scriptTextArea.string = concat.toString()
        if (l?.lineNumber != null) {
            scriptTextArea.textFgColor = intArrayOf(15, 15, 14, 15, 15, 15)
            scriptTextArea.textBgColor = intArrayOf(0, 0, 3, 0, 0, 0)
        } else {
            scriptTextArea.textFgColor = intArrayOf(15)
            scriptTextArea.textBgColor = intArrayOf(0)
        }
        scriptTextArea.transparentBg = false
    }

    fun render(isShown: Boolean, deltaTime: Float) {
        val transitionTarget = if (isShown) 1f else 0f
        transition = MathUtils.clamp(0f, 1f,
            transition + MathUtils.sign(transitionTarget - 0.5f) * TRANSITION_SPEED * deltaTime)

        val boxPosX = MathUtils.lerp(screenDimensions.x(), screenDimensions.x() - box.computedDimensions.x() - 20,
            MathUtils.smoothstep(transition))
        rootPosition.set(boxPosX, screenDimensions.y() * 0.5f)
        box.rootUpdatePosition(screenDimensions, rootPosition, rootAnchor)

        box.render(screenDimensions)
    }

    private companion object {
        const val TRANSITION_SPEED: Float = 6f
    }
}
