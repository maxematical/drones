package drones

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
        glShaderSource(glShaderObject, sourceCode.code)
        glCompileShader(glShaderObject)

        val success = IntArray(1)
        glGetShaderiv(glShaderObject, GL_COMPILE_STATUS, success)
        if (success[0] == GL_FALSE) {
            val message = glGetShaderInfoLog(glShaderObject)
            throw IllegalStateException("Couldn't compile shader '$filename'. Message:\n$message")
        }
    }

    companion object {
        private val includeRegex = Regex("^#include [<\"](.+?)[>\"]")
        private val includedFilesCache = mutableMapOf<String, ShaderSource>()

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

        private fun loadShaderFile(filename: String): ShaderSource {
            val instr = Main::class.java.getResourceAsStream(filename)
                ?: throw FileNotFoundException("Could not find shader '$filename'")

            val reader = BufferedReader(InputStreamReader(instr))

            val sb = StringBuilder()
            var line: String?

            val sections = mutableListOf<ShaderSource.Section>()
            var currentSection = ShaderSource.Section(filename, 1)
            var lineNumber = 0

            while (true) {
                lineNumber++
                line = reader.readLine()
                if (line == null)
                    break

                // Try to include file
                // TODO: Obey preprocessor directives when including a file
                val match = includeRegex.matchEntire(line)
                if (match != null) {
                    val includeFilename = match.groupValues[1]

                    // Try to get included file from cache, otherwise read it from disk
                    val includedSource: ShaderSource
                    if (includeFilename in includedFilesCache) {
                        includedSource = includedFilesCache[includeFilename]!!
                    } else {
                        includedSource = loadShaderFile("/glsl/$includeFilename")
                        includedFilesCache[includeFilename] = includedSource
                    }

                    // Add new section for the included source
                    currentSection.lastLine = lineNumber - 1
                    sections.add(currentSection)

                    currentSection = ShaderSource.Section("/glsl/$includeFilename", lineNumber)
                    lineNumber += includedSource.totalLength + 1
                    currentSection.lastLine = lineNumber - 1
                    sections.add(currentSection)

                    // Make another section for the rest of this file
                    currentSection = ShaderSource.Section(filename, lineNumber)

                    // Note the section that is from the included file
                    line = includedSource.code.trim()

                    // For some reason we have to increment the line number again
                    lineNumber++
                } else {
                    sb.append("/*  ${currentSection.sourceFile}:${lineNumber - currentSection.startLine + 1}".padEnd(30) + " */ ")
                }

                sb.append(line)
                sb.append('\n')
            }

            currentSection.lastLine = lineNumber - 1
            sections.add(currentSection)

            return ShaderSource(sb.toString(), sections)
        }
    }

    private class ShaderSource(val code: String, val sections: List<Section>) {
        val totalLength = sections.sumBy { it.length }

        fun getSourceFile(line: Int): String {
            for (section in sections) {
                if (section.startLine >= line && section.lastLine <= line) {
                    return section.sourceFile
                }
            }
            return "???"
        }

        fun getSourceLine(line: Int): Int {
            for (section in sections) {
                if (section.startLine >= line && section.lastLine <= line) {
                    return line - section.startLine + 1
                }
            }
            return -1
        }

        class Section(val sourceFile: String, val startLine: Int) {
            var lastLine: Int = -1
            val length: Int get() = lastLine - startLine + 1
        }
    }
}
