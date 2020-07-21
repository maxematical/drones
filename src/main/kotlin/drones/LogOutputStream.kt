package drones

import org.slf4j.Logger
import org.slf4j.event.Level
import java.io.OutputStream

class LogOutputStream(private val logger: Logger, private val logLevel: Level,
                      private val additionalOutput: MutableList<String>? = null,
                      initialBufferSize: Int = DEFAULT_BUFFER_SIZE) : OutputStream() {
    private var buffer: ByteArray = ByteArray(initialBufferSize)
    private var nextBufferIndex: Int = 0
    private var bufferModified = false

    override fun write(b: Int) {
        // Ignore carriage returns
        if (b.toChar() == '\r')
            return

        // Replace tab characters with two spaces
        if (b.toChar() == '\t') {
            write(' '.toInt())
            write(' '.toInt())
            return
        }

        // Process the character
        bufferModified = true
        if (b.toChar() == '\n') {
            flush()
        } else {
            if (nextBufferIndex == buffer.size) {
                val newBuffer = ByteArray(buffer.size * 2)
                System.arraycopy(buffer, 0, newBuffer, 0, buffer.size)
                buffer = newBuffer
            }
            buffer[nextBufferIndex++] = (b and 255).toByte()
        }
    }

    override fun flush() {
        if (!bufferModified)
            return

        val str = String(buffer, 0, nextBufferIndex)
        logger.log(str, logLevel)
        additionalOutput?.add(str)
        nextBufferIndex = 0
        bufferModified = false
    }

    companion object {
        const val DEFAULT_BUFFER_SIZE = 2048
    }
}

private fun Logger.log(msg: String, level: Level) {
    when (level) {
        Level.TRACE -> trace(msg)
        Level.DEBUG -> debug(msg)
        Level.INFO -> info(msg)
        Level.WARN -> warn(msg)
        Level.ERROR -> error(msg)
    }
}
