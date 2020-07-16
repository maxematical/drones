package drones

import org.lwjgl.opengl.GL30.*
import org.lwjgl.stb.STBImage
import org.lwjgl.stb.STBImage.stbi_load_from_memory
import org.lwjgl.system.MemoryStack
import java.io.FileNotFoundException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.channels.Channels
import java.nio.channels.ReadableByteChannel
import javax.xml.bind.JAXBContext
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement
import kotlin.math.abs
import kotlin.math.min

/**
 * Loads a font's bitmap texture, parses its XML file, and returns a GameFont object ready to be used for rendering.
 *
 * It is expected that a PNG bitmap and an XML meta file will be in the resources folder, with file names
 * `FAMILY/FAMILY_STYLE_SIZE.(png|xml)`, respectively.
 *
 * @param family the name of the font family
 * @param style the name of the font style used in the bitmap image's name
 * @param size the size (line height) of the font in the bitmap
 */
fun loadFont(family: String, style: String, size: Int): GameFont {
    val fontpath = "/fonts/${family.toLowerCase()}/${family.toLowerCase()}_${style.toLowerCase()}_$size"

    val xmlStream = getFontXmlStream(fontpath + ".xml")

    val ctx = JAXBContext.newInstance(XmlFontEntry::class.java)
    val unmarshaller = ctx.createUnmarshaller()

    val font = unmarshaller.unmarshal(xmlStream) as XmlFontEntry
    val chars = font.children

    val characterLut = CharArray(128)
    val characterCoordinatesLut = IntArray(128)
    val characterOffsetLut = IntArray(128)
    val characterWidthLut = IntArray(128)
    val characterCodeLut = mutableMapOf<Char, Int>()

    for (idx in 0..min(127, chars.lastIndex)) {
        characterLut[idx] = chars[idx].code[0]

        val rect = chars[idx].rect.split(' ')
        if (rect.size != 4)
            throw RuntimeException("Invalid rect attribute for character '${chars[idx].code}'")

        val offset = chars[idx].offset.split(' ')
        if (offset.size != 2)
            throw RuntimeException("Invalid offset attribute for character '${chars[idx].code}")

        val characterWidth = chars[idx].width.toInt()
        characterWidthLut[idx] = characterWidth
        val offsetX = offset[0].toInt()
        val offsetY = offset[1].toInt()

        val uvX = rect[0].toInt()
        val uvY = rect[1].toInt()
        val uvWidth = rect[2].toInt()
        val uvHeight = rect[3].toInt()

        if (uvX >= 512 || uvY >= 512 || uvWidth >= 128 || uvHeight >= 128)
            println("Warning: Character '${chars[idx].code}' may not display properly with this font because the " +
                    "bitmap is too large")

        characterCoordinatesLut[idx] = (uvX shl 23) or
                (uvY shl 14) or
                (uvWidth shl 7) or
                uvHeight

        characterOffsetLut[idx] = ((if (offsetX < 0) 1 else 0) shl 17) or
                ((if (offsetY < 0) 1 else 0) shl 16) or
            ((abs(offsetX) and 255) shl 8) or
            (abs(offsetY) and 255)

        characterCodeLut[chars[idx].code[0]] = idx
    }

    if (' ' in characterCodeLut) {
        val spaceCode = characterCodeLut[' ']!!
        characterCoordinatesLut[spaceCode] = 0
        characterOffsetLut[spaceCode] = 0
    }

    val (bitmapWidth, bitmapHeight, bitmapData) = loadFontBitmap(fontpath + ".png")

    val glBitmap = glGenTextures()
    glBindTexture(GL_TEXTURE_2D, glBitmap)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, bitmapWidth, bitmapHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, bitmapData)
    glGenerateMipmap(GL_TEXTURE_2D)

    return GameFont(family.toLowerCase().capitalize(),
        glBitmap, bitmapData, bitmapWidth, bitmapHeight, font.height,
        characterLut, characterCodeLut, characterCoordinatesLut, characterOffsetLut, characterWidthLut)
}

private fun getFontXmlStream(filename: String): InputStream =
    Main::class.java.getResourceAsStream(filename)
        ?: throw FileNotFoundException("Could not find font '$filename'")

private fun loadFontBitmap(filename: String): Triple<Int, Int, ByteBuffer> {
    return Main::class.java.getResourceAsStream(filename).use { instr: InputStream? ->
        if (instr == null) error("Bitmap image not found, filename: '$filename'")

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
            val bytes = stbi_load_from_memory(buffer, w, h, nchannels, 4)
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

@XmlRootElement(name = "Font")
class XmlFontEntry {
    @JvmField @XmlAttribute var height: Int = 0

    @XmlElement(name = "Char")
    @JvmField
    var children: MutableList<XmlCharEntry> = mutableListOf()
}

data class XmlCharEntry(@JvmField @XmlAttribute var width: String = "",
                        @JvmField @XmlAttribute var offset: String = "",
                        @JvmField @XmlAttribute var rect: String = "",
                        @JvmField @XmlAttribute var code: String = "")

class GameFont(val name: String,
               val glBitmap: Int,
               val bitmapTexture: ByteBuffer,
               val bitmapWidth: Int,
               val bitmapHeight: Int,
               val lineHeight: Int,
               /** Look up character by Glyph ID */
               val characterLut: CharArray,
               /** Look up Glyph ID by character */
               val characterCodeLut: Map<Char, Int>,
               /** Look up packed UV coordinates by Glyph ID */
               val characterCoordinatesLut: IntArray, // length of array is 128
               /** Look up packed character offset by Glyph ID */
               val characterOffsetLut: IntArray,
               /** Look up character width by Glyph ID */
               val characterWidthLut: IntArray)
