package drones.ui

import org.joml.Vector2fc

interface Layout {
    val dimensions: Vector2fc
    val anchorPoint: Vector2fc
    val position: Vector2fc

    fun updateLayout()
}
