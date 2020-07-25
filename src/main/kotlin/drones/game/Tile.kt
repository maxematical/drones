package drones.game

import drones.MathUtils

abstract class Tile {
    abstract val name: String

    open val initialMetadata: Int = 0
    open val isCollidable: Boolean = true

    abstract fun getAppearance(metadata: Int): Char
    open fun getBackgroundColor(metadata: Int): Int = 0
    open fun getForegroundColor(metadata: Int): Int = 8
    open fun getTooltip(metadata: Int): String = name
}

object TileAir : Tile() {
    override val name: String = "Air"
    override val isCollidable: Boolean = false

    override fun getAppearance(metadata: Int): Char = ' '
}

object TileOre : Tile() {
    override val name: String = "Ore"
    override val initialMetadata: Int = 8

    override fun getAppearance(metadata: Int): Char = 'X'
    override fun getBackgroundColor(metadata: Int): Int = metadata / 2
    override fun getForegroundColor(metadata: Int): Int = 9

    override fun getTooltip(metadata: Int) = "Ore ($metadata/$initialMetadata Left)"

    /**
     * Mines part of this tile, returns the new tile data here and the kg of ore extracted. (may change the actual tile)
     */
    fun mineTile(metadata: Int): Pair<Int, Double> {
        val newMetadata = MathUtils.clamp(0, 8, metadata - 1)
        val newTile = if (newMetadata > 0) this else TileAir
        val kgExtracted = 1.0

        val newData = TileManager.calcData(newTile, newMetadata)
        return newData to kgExtracted
    }
}

object TileStone : Tile() {
    override val name: String = "Stone"

    override fun getAppearance(metadata: Int): Char = ' '
    override fun getBackgroundColor(metadata: Int): Int = 8
}

object TileManager {
    private val tileMap = mutableMapOf<Int, Tile>()
    private var invTileMap: Map<Tile, Int> = emptyMap()

    fun registerTiles() {
        tileMap[0] = TileAir
        tileMap[1] = TileOre
        tileMap[2] = TileStone
        invTileMap = tileMap.entries.associateBy({ (_, v) -> v }, { (k, _) -> k })
    }

    fun getTileFromId(id: Int): Tile? = tileMap[id]

    fun getId(tile: Tile): Int = invTileMap[tile] ?: error("Tile $tile not registered to TileManager")

    fun getTileFromData(data: Int): Tile? =
        getTileFromId(data and 255)

    fun getMetadata(data: Int): Int =
        (data shr 8)

    fun setTile(data: Int, tile: Tile): Int {
        // Mask out the old tile data
        var data2 = data and 255.inv()
        // Add in new tile data
        data2 = data2 or getId(tile)

        return data2
    }

    fun setMetadata(data: Int, metadata: Int): Int {
        if ((metadata and 16777215) != metadata) {
            error("Metadata doesn't fit in 24 bits: $metadata")
        }
        return (data and 255) or (metadata shl 8)
    }

    fun calcData(tile: Tile, metadata: Int): Int =
        setMetadata(getId(tile), metadata)
}
