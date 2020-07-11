package drones.ui

import org.joml.Vector2fc

interface Container {
    /** Contains the width and height of the container, in pixels. */
    val dimensions: Vector2fc

    /** Contains the bottom left coordinate of the container, in pixels. */
    val bottomLeft: Vector2fc

    /** The width of the container, in pixels. */
    val width: Float get() = dimensions.x()
    /** The height of the container, in pixels. */
    val height: Float get() = dimensions.y()
}
