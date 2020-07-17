package drones.game

import org.joml.Vector2fc

data class ScanRequest(
    val position: Vector2fc,
    val radius: Int
)
