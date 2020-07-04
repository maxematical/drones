package drones

import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL20.*
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.InputStreamReader

class Shader private constructor(filename: String, glType: Int) {
    val filename: String
    val glType: Int
    val glShaderObject: Int

    init {
        this.filename = filename
        this.glType = glType

        val sourceCode = loadShaderFile(filename)

        glShaderObject = glCreateShader(glType)
        glShaderSource(glShaderObject, sourceCode)
        glCompileShader(glShaderObject)

        val success = IntArray(1)
        glGetShaderiv(glShaderObject, GL_COMPILE_STATUS, success)
        if (success[0] == GL_FALSE) {
            val message = glGetShaderInfoLog(glShaderObject)
            throw IllegalStateException("Couldn't compile shader '$filename'. Message:\n$message")
        }
    }

    companion object {
        fun create(filename: String, glType: Int): Shader =
            Shader(filename, glType)

        fun createProgram(shader1: Shader, shader2: Shader): Int {
            val program = glCreateProgram()
            glAttachShader(program, shader1.glShaderObject)
            glAttachShader(program, shader2.glShaderObject)
            glLinkProgram(program)
            glDetachShader(program, shader1.glShaderObject)
            glDetachShader(program, shader2.glShaderObject)

            if (glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE) {
                val message = glGetProgramInfoLog(program)
                throw IllegalStateException("Couldn't link program. Message:\n$message")
            }

            return program
        }

        private fun loadShaderFile(filename: String): String {
            val instr = Main::class.java.getResourceAsStream(filename)
                ?: throw FileNotFoundException("Could not find shader '$filename'")

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
    }
}
