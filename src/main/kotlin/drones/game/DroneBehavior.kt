package drones.game

import drones.*
import org.joml.Vector2f

class DroneBehavior(private val gameState: GameState, private val drone: Drone) : Behavior {
    private val body get() = drone.physics.physicsBody
    private var carryingBeam: LaserBeam? = null

    override fun update(deltaTime: Float) {
        val accel: Vector2f = Vector2f(drone.desiredVelocity).sub(body.linearVelocity.toJoml())
        if (accel.lengthSquared() > 0 && accel.lengthSquared() > (50f * 50f * deltaTime * deltaTime))
            accel.normalize().mul(50f * deltaTime)

        body.applyForce(accel.toDyn4j())

        if (body.linearVelocity.magnitudeSquared > 1) {
            body.linearVelocity.normalize()
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
            laser.behavior = TractorBeamBehavior(gameState, laser, drone.physics.physicsBody, carrying.carryingBody)
            gameState.spawnQueue.add(laser)
            carryingBeam = laser
        }
        if (carrying == null && carryingBeam != null) {
            gameState.despawnQueue.add(carryingBeam)
            carryingBeam = null
        }
    }
}

class CreateDroneBehavior(private val drone: Drone) : CreateBehavior {
    override fun create(state: GameState): Behavior =
        DroneBehavior(state, drone)
}
