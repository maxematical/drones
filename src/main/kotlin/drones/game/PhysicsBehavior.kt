package drones.game

import org.dyn4j.dynamics.Body
import org.dyn4j.geometry.Vector2

class PhysicsBehavior(private val obj: GameObject,
                      val physicsBody: Body, private val physicsSlowing: Double = 0.0) {
    private val slowingForce = Vector2()
    private var spawnedBody = false

    fun update(gameState: GameState, deltaTime: Float) {
        if (!spawnedBody) {
            physicsBody.userData = obj
            gameState.world.addBody(physicsBody)
            spawnedBody = true
        }

        obj.position.set(physicsBody.transform.translation.x, physicsBody.transform.translation.y)

        val slowX = Math.min(physicsSlowing / deltaTime, Math.abs(physicsBody.linearVelocity.x))
        val slowY = Math.min(physicsSlowing / deltaTime, Math.abs(physicsBody.linearVelocity.y))

        slowingForce.set(-Math.signum(physicsBody.linearVelocity.x) * slowX,
            -Math.signum(physicsBody.linearVelocity.y) * slowY)
        physicsBody.applyForce(slowingForce)
    }

    fun destroy(gameState: GameState) {
        if (spawnedBody) {
            gameState.world.removeBody(physicsBody)
            spawnedBody = false
        }
    }
}
