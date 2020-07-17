package drones.game

/**
 * "Component" for a GameObject responsible for interacting with the external world.
 */
interface Behavior {
    fun update(deltaTime: Float)

    fun destroy() {}
}

interface CreateBehavior {
    fun create(state: GameState): Behavior
}
