package drones.game

import drones.*
import org.joml.Vector2f

class DroneBehavior(private val gameState: GameState, private val drone: Drone) : EntityBehavior {
    private var carryingBeam: LaserBeam? = null

    override fun update(deltaTime: Float) {
        val accel: Vector2f = Vector2f(drone.desiredVelocity).sub(drone.physicsBody.linearVelocity.toJoml())
        if (accel.lengthSquared() > 0 && accel.lengthSquared() > (50f * 50f * deltaTime * deltaTime))
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
        drone.miningBeam?.let { laser -> if (!laser.spawned) gameState.spawnQueue.add(laser) }

        val carrying = drone.carryingObject
        if (carrying != null && carryingBeam == null) {
            val laser = LaserBeam(drone.position, 0f, 0.75f, 5f)
            laser.colorR = 1.8f * 0.185f
            laser.colorG = 1.8f * 1.0f
            laser.colorB = 1.8f * 0.6f
            laser.behavior = TractorBeamBehavior(gameState, laser, drone.position, carrying.position,
                drone.physicsBody, carrying.physicsBody!!)
            gameState.spawnQueue.add(laser)
            carryingBeam = laser
        }
        if (carrying == null && carryingBeam != null) {
            gameState.despawnQueue.add(carryingBeam)
            carryingBeam = null
        }
    }
}
