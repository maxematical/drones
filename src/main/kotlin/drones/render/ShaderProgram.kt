package drones.render

import drones.Main
import org.lwjgl.opengl.GL20.*
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.InputStreamReader

// Whether to add information about which file each line of a GLSL program is from
const val ADD_INCLUDE_DEBUG_INFO = true

class Shader private constructor(filename: String, glType: Int) {
    val filename: String
    val glType: Int
    val glShaderObject: Int

    init {
        this.filename = filename
        this.glType = glType

        val source = loadShaderFile(filename)

        glShaderObject = glCreateShader(glType)
        glShaderSource(glShaderObject, source.sourceCode)
        glCompileShader(glShaderObject)

        val success = IntArray(1)
        glGetShaderiv(glShaderObject, GL_COMPILE_STATUS, success)
        if (success[0] == GL_FALSE) {
            val rawMessage = glGetShaderInfoLog(glShaderObject)
            val message = processMessage(rawMessage, source)
            throw IllegalStateException("Couldn't compile shader '$filename'. Message:\n$message")
        }
    }

    /**
     * Replaces line numbers in the error message so that in shaders with #includes, it is possible to find which exact
     * file had the error.
     */
    private fun processMessage(message: String, source: ShaderSource): String =
        message.replace(errorMessageRegex) { matchResult ->
            // The first group value (groupValues[1]) refers to the index of the source code provided to glShaderSource
            // Not actually used here, but see https://stackoverflow.com/a/48162000 for more info

            val sourceIndex = matchResult.groupValues[1]
            val lineNumber = matchResult.groupValues[2].toInt()

            val fromFile = source.getSourceFile(lineNumber).substring("/glsl/".length)
            val fromLine = source.getSourceLine(lineNumber)

            "ERROR: $sourceIndex:$fromFile:$fromLine:"
        }

    companion object {
        private val includeRegex = Regex("^#include [<\"](.+?)[>\"]")
        private val errorMessageRegex = Regex("ERROR: (\\d+):(\\d+):")
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
            var currentSection = ShaderSource.Section(filename, 1, 1)
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
                    val oldSection = currentSection

                    currentSection = ShaderSource.Section("/glsl/$includeFilename", lineNumber, 1)
                    lineNumber += includedSource.totalLength
                    currentSection.lastLine = lineNumber - 1
                    sections.add(currentSection)

                    // Make another section for the rest of this file
                    currentSection = ShaderSource.Section(
                        filename, lineNumber,
                        oldSection.startLineInFile + oldSection.length + 1
                    )

                    // Note the section that is from the included file
                    line = includedSource.sourceCode.trim()

                    // For some reason we have to increment the line number again
                    lineNumber++
                } else if (ADD_INCLUDE_DEBUG_INFO) {
                    sb.append("/*  ")
                        .append("${currentSection.sourceFile}:${lineNumber - currentSection.startLine + 1}".padEnd(30))
                        .append(" */ ")
                }

                sb.append(line)
                sb.append('\n')
            }

            currentSection.lastLine = lineNumber - 1
            sections.add(currentSection)

            return ShaderSource(sb.toString(), sections)
        }
    }

    private class ShaderSource(val sourceCode: String, val sections: List<Section>) {
        val totalLength = sections.sumBy { it.length }

        fun getSourceFile(line: Int): String {
            for (section in sections) {
                if (line >= section.startLine && line <= section.lastLine) {
                    return section.sourceFile
                }
            }
            return "???"
        }

        fun getSourceLine(line: Int): Int {
            for (section in sections) {
                if (line >= section.startLine && line <= section.lastLine) {
                    return section.startLineInFile + line - section.startLine
                }
            }
            return -1
        }

        class Section(val sourceFile: String, val startLine: Int, val startLineInFile: Int) {
            var lastLine: Int = -1
            val length: Int get() = lastLine - startLine + 1
        }
    }
}
