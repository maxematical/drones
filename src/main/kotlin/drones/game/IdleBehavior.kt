package drones.game

import org.dyn4j.geometry.Vector2

class IdleBehavior(private val obj: GameObject) : EntityBehavior {
    private val slowingForce = Vector2()

    override fun update(deltaTime: Float) {
        obj.physicsBody?.let { body ->
            obj.position.set(body.transform.translation.x, body.transform.translation.y)

            val slowX = Math.min(obj.physicsSlowing / deltaTime, Math.abs(body.linearVelocity.x))
            val slowY = Math.min(obj.physicsSlowing / deltaTime, Math.abs(body.linearVelocity.y))

            slowingForce.set(-Math.signum(body.linearVelocity.x) * slowX,
                -Math.signum(body.linearVelocity.y) * slowY)
            body.applyForce(slowingForce)
        }
    }
}
