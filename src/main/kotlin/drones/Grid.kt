package drones

import org.joml.Vector2f
import org.joml.Vector2fc
import kotlin.math.floor

class Grid(val width: Int, val height: Int, val positionTopLeft: Vector2fc = Vector2f(-width / 2f, -height / 2f)) {
    val tiles: Array<Array<Tile>> = Array(height) { Array<Tile>(width) { TileAir } }

    fun getNearestTile(x: Float, y: Float): Tile {
        val gridX: Int = floor(x - positionTopLeft.x()).toInt()
        val gridY: Int = floor(height - y - positionTopLeft.y()).toInt()

        return tiles.getOrNull(gridY)?.getOrNull(gridX) ?: TileAir
    }

    fun toBitmapArray(font: GameFont): IntArray {
        val arr = IntArray(width * height)

        for (y in tiles.indices) {
            for (x in tiles[y].indices) {
                val tile = tiles[y][x]

                val charCode = font.characterCodeLut[tile.appearance] ?: error("Font doesn't support appearance" +
                        "'${tile.appearance}' for tile ${tile}")
                val bgColor = tile.backgroundColor
                val fgColor = tile.foregroundColor

                val arrIndex = x + y * width
                arr[arrIndex] = (charCode and 255) or
                        ((bgColor and 255) shl 8) or
                        ((fgColor and 255) shl 16)
            }
        }

        return arr
    }
}