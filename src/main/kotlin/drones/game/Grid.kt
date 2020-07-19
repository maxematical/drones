package drones.game

import drones.GameFont
import org.dyn4j.dynamics.Body
import org.dyn4j.dynamics.BodyFixture
import org.dyn4j.geometry.MassType
import org.dyn4j.geometry.Rectangle
import org.joml.Vector2f
import org.joml.Vector2fc
import org.joml.Vector2i
import org.joml.Vector2ic
import org.slf4j.LoggerFactory
import kotlin.math.floor

class Grid(val width: Int, val height: Int, val positionTopLeft: Vector2fc = Vector2f(-width / 2f, height / 2f)) {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val tileData: Array<Array<Int>> = Array(height) { Array<Int>(width) { 0 } }
    private val fixtureIndices: Array<Array<Int>> = Array(height) { Array<Int>(width) { -1 } }

    val physicsBody: Body

    init {
        val predictedNumberCollidableTiles: Int = (width * height * 0.15f).toInt()
        physicsBody = Body(predictedNumberCollidableTiles)
        physicsBody.setMass(MassType.INFINITE)
    }

    fun worldToGridX(worldX: Float): Int =
        floor(worldX - positionTopLeft.x()).toInt()

    fun worldToGridY(worldY: Float): Int =
        -floor(worldY - positionTopLeft.y()).toInt()

    fun worldToGrid(position: Vector2fc): Vector2ic =
        Vector2i(worldToGridX(position.x()), worldToGridY(position.y()))

    fun gridToWorldX(gridX: Int): Float =
        positionTopLeft.x() + gridX

    fun gridToWorldY(gridY: Int): Float =
        positionTopLeft.y() - gridY

    fun getTile(gridX: Int, gridY: Int): Tile {
        val data = tileData[gridY][gridX]
        return TileManager.getTileFromData(data) ?: error("Corrupt tile data at grid position ($gridX,$gridY): $data")
    }

    fun getMeta(gridX: Int, gridY: Int): Int {
        val data = tileData[gridY][gridX]
        return TileManager.getMetadata(data)
    }

    fun setTile(gridX: Int, gridY: Int, tile: Tile, newMetadata: Int = tile.initialMetadata) {
        setData(gridX, gridY, TileManager.calcData(tile, newMetadata))
    }

    fun setMeta(gridX: Int, gridY: Int, metadata: Int) {
        setData(gridX, gridY, TileManager.setMetadata(tileData[gridY][gridX], metadata))
    }

    fun setData(gridX: Int, gridY: Int, data: Int) {
        updateTileFixture(gridX, gridY, data)
        tileData[gridY][gridX] = data
    }

    /**
     * Called before tileData is modified. Checks whether the tile is being replaced and either the old tile didn't
     * need collisions and the new one does, or vice versa. If so, updates the physics body to reflect these changes.
     */
    private fun updateTileFixture(gridX: Int, gridY: Int, newData: Int) {
        val currentTile = getTile(gridX, gridY)
        val newTile = TileManager.getTileFromData(newData) ?: error("Invalid data $newData")

        if (!currentTile.isCollidable && newTile.isCollidable) {
            // Add a fixture for this tile
            logger.info("Tile at grid position ($gridX, $gridY) changed from $currentTile to $newTile. Adding " +
                    "collision fixture here.")

            val rectangle = Rectangle(1.0, 1.0)
            rectangle.translate(gridToWorldX(gridX) + 0.5, gridToWorldY(gridY) - 0.5)

            val fixture = BodyFixture(rectangle)
            fixture.userData = Vector2i(gridX, gridY)

            val fixtureIndex = physicsBody.fixtureCount
            physicsBody.addFixture(fixture)
            fixtureIndices[gridY][gridX] = fixtureIndex

            // Double-check that the fixture index is correct
            if (physicsBody.getFixture(fixtureIndex) != fixture)
                throw RuntimeException("Program bug: grid fixture inserted was not at the expected index")
        }
        if (currentTile.isCollidable && !newTile.isCollidable) {
            // Remove a fixture for this tile
            logger.info("Tile at grid position ($gridX, $gridY) changed from $currentTile to $newTile. Removing " +
                    "collision fixture here.")

            val fixtureIndex = fixtureIndices[gridY][gridX]
            if (fixtureIndex == -1)
                throw RuntimeException("Couldn't remove fixture at grid pos ($gridX, $gridY), fixture index not exist")

            physicsBody.removeFixture(fixtureIndex)
            fixtureIndices[gridY][gridX] = -1
        }
    }

    fun toBitmapArray(font: GameFont): IntArray {
        val arr = IntArray(width * height)

        for (y in tileData.indices) {
            for (x in tileData[y].indices) {
                val tile = getTile(x, y)
                val meta = getMeta(x, y)

                val appearance = tile.getAppearance(meta)
                val charCode = font.characterCodeLut[appearance] ?: error("Font doesn't support appearance" +
                        "'$appearance' for tile $tile")
                val bgColor = tile.getBackgroundColor(meta)
                val fgColor = tile.getForegroundColor(meta)

                val arrIndex = x + y * width
                arr[arrIndex] = (charCode and 255) or
                        ((bgColor and 255) shl 8) or
                        ((fgColor and 255) shl 16)
            }
        }

        return arr
    }
}
