package drones.game

import org.joml.Vector4fc

interface Hoverable {
    /** Returns whether the mouse is hovered over the game object.
     * @param mousePos The mouse position in world coordinates. Z and W components can be ignored */
    fun isHover(mousePos: Vector4fc): Boolean
}
