package drones.game

import drones.*
import drones.scripting.ModuleComms
import drones.scripting.ModuleScanner
import drones.scripting.ScriptManager
import org.joml.Vector2f
import org.luaj.vm2.LuaValue
import java.lang.Float.min

class DroneBehavior(private val gameState: GameState, private val drone: Drone) : Behavior {
    private val body get() = drone.physics.physicsBody
    private var carryingBeam: LaserBeam? = null

    private var currentRotationSpeed: Float = 0f
    private val maxRotationAccel: Float = 180f

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
                drone.hasDestination = false
                drone.desiredVelocity.set(0f, 0f)
            }
        }

        // Apply thrust force in the desired direction
        val accel: Vector2f = Vector2f(drone.desiredVelocity).sub(body.linearVelocity.toJoml())

        // Determine how much acceleration is needed to reach the desired velocity, then consume power and account for
        // the actual amount of power we have to perform the acceleration
        val desiredAccel: Float =
            if (accel.lengthSquared() > (50f * 50f)) 50f
            else if (Math.abs(accel.lengthSquared()) < 0.001f) 0f
            else accel.length()
        val desiredThrustPower = PowerConstants.THRUST_CONSTANT * desiredAccel * deltaTime
        val actualThrustPower = drone.consumePower(desiredThrustPower)
        val actualAccel = actualThrustPower / PowerConstants.THRUST_CONSTANT / deltaTime

        // Apply the desired thrust force to the physics body
        if (Math.abs(accel.lengthSquared()) > 0.001f)
            accel.normalize().mul(actualAccel.toFloat())
        body.applyForce(accel.toDyn4j())

        // Prevent the drone from moving too fast
        if (body.linearVelocity.magnitudeSquared > 1) {
            body.linearVelocity.normalize()
        }

        // Regenerate power, if possible
        if (drone.localTime - drone.lastPowerConsumedTime >= PowerConstants.RECHARGE_DELAY) {
            val addPower = PowerConstants.RECHARGE_PER_SECOND * deltaTime
            drone.currentPower = Math.min(drone.currentPower + addPower, drone.maxPower)
        }

        // Update drone rotation
        updateRotation(deltaTime)

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
        ModuleComms.processBroadcastQueue(gameState, drone)

        if (drone.activeScanning && !script.isRunningCallback) {
            val callback = ModuleScanner.updateActiveScanning(gameState, drone, script.globals)
            if (callback != null)
                return callback
        }
        if (drone.isListeningForComms && !script.isRunningCallback) {
            val callback = ModuleComms.listenForSignals(gameState, drone, script.globals)
            if (callback != null)
                return callback
        }

        return null
    }

    private fun updateRotation(deltaTime: Float) {
        // Compute desired rotation (point in the direction of the current velocity)
        val desiredRotation: Float
        if (drone.desiredVelocity.lengthSquared() > 0.0625f) {
            desiredRotation = MathUtils.RAD2DEG *
                    Math.atan2(drone.desiredVelocity.y.toDouble(), drone.desiredVelocity.x.toDouble()).toFloat()
        } else {
            desiredRotation = drone.rotation
        }

        // Compute desired angular velocity
        val deltaRotation = MathUtils.clampRotation(desiredRotation - drone.rotation)
        val desiredRotationSpeed = 150f * Math.signum(deltaRotation) * Math.min(1f, Math.abs(deltaRotation) / 60f)

        // Compute angular acceleration needed to obtain that velocity
        val deltaRotationSpeed = MathUtils.clampRotation(desiredRotationSpeed - currentRotationSpeed)
        val desiredRotationAccel = Math.signum(deltaRotationSpeed) *
                Math.min(Math.abs(deltaRotationSpeed), deltaTime * maxRotationAccel)

        // If there is a desired acceleration, then spend some power to perform the acceleration and modify the velocity
        if (Math.abs(desiredRotationAccel) > 0.001f) {
            val desiredRotationPower = Math.abs(desiredRotationAccel) * PowerConstants.ROTATION_CONSTANT
            val actualRotationPower = drone.consumePower(desiredRotationPower)
            val actualRotationAccel: Float = desiredRotationAccel *
                    (actualRotationPower / desiredRotationPower).toFloat()

            currentRotationSpeed += actualRotationAccel
        }

        // Change rotation by the current angular velocity
        drone.rotation += currentRotationSpeed * deltaTime
    }
}

class CreateDroneBehavior(private val drone: Drone) : CreateBehavior {
    override fun create(state: GameState): Behavior =
        DroneBehavior(state, drone)
}
