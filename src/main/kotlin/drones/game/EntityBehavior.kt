package drones.game

/**
 * "Component" for a GameObject responsible for interacting with the external world.
 */
interface EntityBehavior {
    fun update(deltaTime: Float)
}
