package drones

import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL30.*
import org.lwjgl.opengl.GL43.GL_SHADER_STORAGE_BUFFER
import org.lwjgl.stb.STBImage
import org.lwjgl.system.MemoryUtil.*
import java.io.*
import java.nio.ByteBuffer

class Main

fun main(args: Array<String>) {
    println("Hello world")

    // Set up OpenGL
    glfwInit()
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4)
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3)
    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)

    val window: Long = glfwCreateWindow(256, 256, "Hello World", NULL, NULL)
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

    // Create the triangle we want to show on-screen
    val vertices: FloatArray = floatArrayOf(
        -1f, -1f, 0.0f,
        1f, -1f, 0.0f,
        1f, 1f, 0.0f,

        -1f, 1f, 0f,
        -1f, -1f, 0f,
        1f, 1f, 0f
    )

    // Create Vertex Array Object (calls below will also bind things into this object)
    val vao = glGenVertexArrays()
    glBindVertexArray(vao)

    // Create vertex buffer object
    val vbo = glGenBuffers()
    glBindBuffer(GL_ARRAY_BUFFER, vbo)
    glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)

    // Tell OpenGL how to use the vertex attributes for use by the future vertex shader
    glVertexAttribPointer(0, 3, GL_FLOAT, false, 12, 0) // set up vertex position for use in vertex shader
    glEnableVertexAttribArray(0)

    // Set up the shaders
    val vertexShader = glCreateShader(GL_VERTEX_SHADER)
    glShaderSource(vertexShader, loadShader("vertex.vert"))
    glCompileShader(vertexShader)
    checkShaderStatus(vertexShader)?.let { error -> println("Couldn't set up vertex shader: $error") }

    val fragmentShader = glCreateShader(GL_FRAGMENT_SHADER)
    glShaderSource(fragmentShader, loadShader("fragment.frag"))
    glCompileShader(fragmentShader)
    checkShaderStatus(fragmentShader)?.let { error -> println("Couldn't set up fragment shader: $error") }

    val shaderProgram = glCreateProgram()
    glAttachShader(shaderProgram, vertexShader)
    glAttachShader(shaderProgram, fragmentShader)
    glLinkProgram(shaderProgram)

    glUseProgram(shaderProgram)
    glDeleteShader(vertexShader)
    glDeleteShader(fragmentShader)

    // Set up bitmap (font) texture
    val font = loadFont()
//    println(Integer.toBinaryString(font.characterCoordinatesLut[font.characterCodeLut['H']!!])) // <-- this works

    val texture = glGenTextures()
    glBindTexture(GL_TEXTURE_2D, texture)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, font.bitmapWidth, font.bitmapHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, font.bitmapTexture)
    glGenerateMipmap(GL_TEXTURE_2D)

    // Set up alpha blending
    glEnable(GL_BLEND)
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)


    val ssbo = glGenBuffers()
    glBindBuffer(GL_SHADER_STORAGE_BUFFER, ssbo)
    glBufferData(GL_SHADER_STORAGE_BUFFER, 4096, GL_DYNAMIC_DRAW)
    glBufferSubData(GL_SHADER_STORAGE_BUFFER, 0, font.characterCoordinatesLut)
    glBufferSubData(GL_SHADER_STORAGE_BUFFER, 512, font.characterOffsetLut)
    //glBufferSubData(GL_SHADER_STORAGE_BUFFER, 128*4, stringToBitmapArray("1", font))
    glBufferSubData(GL_SHADER_STORAGE_BUFFER, 1024, stringToBitmapArray("Hello World.", font))
    //glBufferData(GL_SHADER_STORAGE_BUFFER, stringToBitmapArray("Hello World"), GL_DYNAMIC_DRAW)
    glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, ssbo)
    glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0)


    println(font.characterCoordinatesLut[stringToBitmapArray("d", font)[1]] shr 24)

    // Loop
    while (!glfwWindowShouldClose(window)) {
        glClearColor(0f, 1f, 0f, 1f)
        glClear(GL_COLOR_BUFFER_BIT)

        glUseProgram(shaderProgram)
        glBindTexture(GL_TEXTURE_2D, texture)
        glBindVertexArray(vao)
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
}

fun loadShader(filename: String): String {
    val instr = Main::class.java.getResourceAsStream("/glsl/$filename")
        ?: throw FileNotFoundException("Could not find shader 'glsl/$filename'")

    val reader = BufferedReader(InputStreamReader(instr))

    val sb = StringBuilder()
    var line: String?
    while (true) {
        line = reader.readLine()
        if (line == null)
            break

        sb.append(line)
        sb.append('\n')
    }

    return sb.toString()
}

fun checkShaderStatus(shader: Int): String? {
    val success = IntArray(1)
    glGetShaderiv(shader, GL_COMPILE_STATUS, success)
    if (success[0] == GL_FALSE) {
        return glGetShaderInfoLog(shader, 512)
    }

    return null
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
    return arr
}
