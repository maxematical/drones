package drones

abstract class Tile {
    abstract val name: String

    abstract val appearance: Char
    open val backgroundColor: Int = 0
    open val foregroundColor: Int = 8
}

object TileAir : Tile() {
    override val name: String = "Air"

    override val appearance: Char = ' '
}

object TileStone : Tile() {
    override val name: String = "Stone"

    override val appearance: Char = 'X'
    override val backgroundColor = 1
}
