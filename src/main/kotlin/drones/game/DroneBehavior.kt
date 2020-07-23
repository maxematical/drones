package drones.game

import drones.*
import drones.scripting.ModuleScanner
import drones.scripting.ScriptManager
import org.joml.Vector2f
import org.luaj.vm2.LuaValue
import java.lang.Float.min

class DroneBehavior(private val gameState: GameState, private val drone: Drone) : Behavior {
    private val body get() = drone.physics.physicsBody
    private var carryingBeam: LaserBeam? = null

    private val navDelta = Vector2f()

    override fun update(deltaTime: Float) {
        // Update scripts
        drone.scriptManager?.let { script ->
            val possibleCallback = updateComponents(script)
            script.update(possibleCallback)
        }

        // Update navigation
        if (drone.hasDestination) {
            navDelta.set(drone.destination).sub(drone.position)

            val thrustX = MathUtils.sign(navDelta.x) * min(1f, Math.abs(navDelta.x) / 1.25f)
            val thrustY = MathUtils.sign(navDelta.y) * min(1f, Math.abs(navDelta.y) / 1.25f)

            drone.desiredVelocity.set(thrustX, thrustY)

            if (navDelta.lengthSquared() <= (drone.destinationTargetDistance * drone.destinationTargetDistance)) {
                println("Reached destination")
                drone.hasDestination = false
                drone.desiredVelocity.set(0f, 0f)
            }
        }

        // Update thrust forces on the physics body
        val accel: Vector2f = Vector2f(drone.desiredVelocity).sub(body.linearVelocity.toJoml())
        if (accel.lengthSquared() > 0 && accel.lengthSquared() > (50f * 50f))
            accel.normalize().mul(50f)

        body.applyForce(accel.toDyn4j())

        if (body.linearVelocity.magnitudeSquared > 1) {
            body.linearVelocity.normalize()
        }

        // Update drone rotation
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

        // Update local time
        drone.localTime += deltaTime

        // Spawn laser beam if necessary
        drone.miningBeam?.let { laser -> if (!laser.spawned) gameState.spawnQueue.add(laser) }

        // Update tractor beam
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
        if ((carrying == null || !carrying.carrying.spawned) && carryingBeam != null) {
            gameState.despawnQueue.add(carryingBeam)
            carryingBeam = null
            drone.carryingObject = null
        }
    }

    /**
     * Updates the drone's components/modules, and possibly returns a lua function that should be run as a callback.
     * Null if there is no callback that needs to be called
     */
    private fun updateComponents(script: ScriptManager) : LuaValue? {
        ModuleScanner.processScanQueue(gameState, drone)

        if (drone.activeScanning && !script.isRunningCallback) {
            val callback = ModuleScanner.updateActiveScanning(gameState, drone, script.globals)
            if (callback != null)
                return callback
        }

        return null
    }
}

class CreateDroneBehavior(private val drone: Drone) : CreateBehavior {
    override fun create(state: GameState): Behavior =
        DroneBehavior(state, drone)
}
