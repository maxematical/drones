package drones

import java.io.FileNotFoundException
import java.io.InputStream
import java.nio.ByteBuffer
import javax.xml.bind.JAXBContext
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement
import kotlin.math.abs
import kotlin.math.min

fun loadFont(): GameFont {
    val xmlStream = getFontXmlStream("consolas/consolas_regular_14.xml")

    val ctx = JAXBContext.newInstance(XmlFontEntry::class.java)
    val unmarshaller = ctx.createUnmarshaller()

    val font = unmarshaller.unmarshal(xmlStream) as XmlFontEntry
    val chars = font.children

    val characterLut = CharArray(128)
    val characterCoordinatesLut = IntArray(128)
    val characterOffsetLut = IntArray(128)
    val characterCodeLut = mutableMapOf<Char, Int>()

    for (idx in 0..min(127, chars.lastIndex)) {
        characterLut[idx] = chars[idx].code[0]

        val rect = chars[idx].rect.split(' ')
        if (rect.size != 4)
            throw RuntimeException("Invalid rect attribute for character '${chars[idx].code}'")

        val offset = chars[idx].offset.split(' ')
        if (offset.size != 2)
            throw RuntimeException("Invalid offset attribute for character '${chars[idx].code}")

        val offsetX = offset[0].toInt()
        val offsetY = offset[1].toInt()

        val characterX = rect[0].toInt()
        val characterY = rect[1].toInt()
        val characterWidth = rect[2].toInt()
        val characterHeight = rect[3].toInt()

        characterCoordinatesLut[idx] = (characterX shl 24) or
                (characterY shl 16) or
                (characterWidth shl 8) or
                characterHeight

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

    val (bitmapWidth, bitmapHeight, bitmap) = readImage(
        "C:\\Users\\ofwar\\Documents\\Programming\\drones\\src\\main\\resources\\fonts\\consolas\\consolas_regular_14.png"
    )

    return GameFont("Consolas",
        bitmap, bitmapWidth, bitmapHeight,
        characterLut, characterCodeLut, characterCoordinatesLut, characterOffsetLut)
}

fun getFontXmlStream(filename: String): InputStream =
    Main::class.java.getResourceAsStream("/fonts/$filename")
        ?: throw FileNotFoundException("Could not find font 'fonts/$filename'")

@XmlRootElement(name = "Font")
class XmlFontEntry {
    @XmlElement(name = "Char")
    @JvmField
    var children: MutableList<XmlCharEntry> = mutableListOf()
}

data class XmlCharEntry(@JvmField @XmlAttribute var offset: String = "",
                        @JvmField @XmlAttribute var rect: String = "",
                        @JvmField @XmlAttribute var code: String = "")

class GameFont(val name: String,
               val bitmapTexture: ByteBuffer,
               val bitmapWidth: Int,
               val bitmapHeight: Int,
               val characterLut: CharArray,
               val characterCodeLut: Map<Char, Int>,
               val characterCoordinatesLut: IntArray, // length of array is 128
               val characterOffsetLut: IntArray) // similar to characterCoordinatesLut
