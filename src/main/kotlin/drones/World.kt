package drones

class World(val width: Int, val height: Int) {
    val grid: Array<Array<Tile>> = Array(height) { Array<Tile>(width) { TileAir } }

    fun toBitmapArray(font: GameFont): IntArray {
        val arr = IntArray(width * height)

        for (y in grid.indices) {
            for (x in grid[y].indices) {
                val tile = grid[y][x]

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
