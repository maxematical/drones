package drones

import org.joml.Vector2f

class DroneBehavior(private val gameState: GameState, private val drone: Drone) : EntityBehavior {
    override fun update(deltaTime: Float) {
        val accel: Vector2f = Vector2f(drone.desiredVelocity).sub(drone.physicsBody.linearVelocity.toJoml())
        if (accel.lengthSquared() > 0)
            accel.normalize().mul(50f * deltaTime)

        drone.physicsBody.applyForce(accel.toDyn4j())
        drone.position.set(drone.physicsBody.transform.translation.x, drone.physicsBody.transform.translation.y)

        if (drone.physicsBody.linearVelocity.magnitudeSquared > 1) {
            drone.physicsBody.linearVelocity.normalize()
        }

        val desiredRotation: Float
        if (drone.desiredVelocity.lengthSquared() > 0.0625f) {
            desiredRotation = MathUtils.RAD2DEG *
                    Math.atan2(drone.desiredVelocity.y.toDouble(), drone.desiredVelocity.x.toDouble()).toFloat()
        } else {
            desiredRotation = drone.rotation
        }
        val deltaRotation = MathUtils.clampRotation(desiredRotation - drone.rotation)

        val desiredRotationSpeed = 150f * Math.min(1f, Math.abs(deltaRotation) / 60f)
        val changeRotation = Math.signum(deltaRotation) *
                Math.min(Math.abs(deltaRotation), deltaTime * desiredRotationSpeed)
        drone.rotation += changeRotation

        drone.localTime += deltaTime

        // Spawn laser beam if necessary
        drone.laserBeam?.let { laser -> if (!laser.spawned) gameState.spawnQueue.add(laser) }
    }
}
