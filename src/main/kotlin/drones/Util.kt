package drones

import org.lwjgl.stb.STBImage
import org.lwjgl.system.MemoryStack
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.channels.Channels
import java.nio.channels.ReadableByteChannel

inline fun <reified T : Any> Any?.takeAs(): T? = if (this is T) this else null

fun loadTexture(filename: String): Triple<Int, Int, ByteBuffer> {
    return Main::class.java.getResourceAsStream(filename).use { instr: InputStream? ->
        if (instr == null) error("Texture not found, filename: '$filename'")

        MemoryStack.stackPush().use { stack ->
            // Taken from https://git.io/JJGjz

            val w = stack.mallocInt(1)
            val h = stack.mallocInt(1)
            val nchannels = stack.mallocInt(1)

            val channel: ReadableByteChannel = Channels.newChannel(instr)
            var buffer = ByteBuffer.allocateDirect(1024 * 8) // 8KB

            // Read bytes from the channel into the buffer
            while (channel.read(buffer) > -1) {
                // Resize the buffer if it is full
                if (!buffer.hasRemaining()) {
                    buffer = resizeBuffer(buffer, buffer.capacity() * 2)
                }
            }
            // Reset position of the buffer, prepare for loading into stbi
            buffer.flip()

            // Extract information from image bytes
            val bytes = STBImage.stbi_load_from_memory(buffer, w, h, nchannels, 4)
                ?: throw RuntimeException("Couldn't load image: ${STBImage.stbi_failure_reason()}")
            Triple(w[0], h[0], bytes)

            // TODO Free the image using stbi_free, after giving the texture to OpenGL
        }
    }
}

private fun resizeBuffer(buffer: ByteBuffer, newCapacity: Int): ByteBuffer {
    val new = ByteBuffer.allocateDirect(newCapacity)
    buffer.flip()
    new.put(buffer)
    return new
}
