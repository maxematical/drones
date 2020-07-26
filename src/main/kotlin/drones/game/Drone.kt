package drones.game

import drones.scripting.ScriptManager
import org.dyn4j.dynamics.Body
import org.dyn4j.geometry.Circle
import org.dyn4j.geometry.Mass
import org.dyn4j.geometry.Vector2
import org.joml.Vector2f
import org.joml.Vector2fc
import java.util.*
import kotlin.collections.HashSet

class Drone(override val position: Vector2f,
            val color: Int = 0xFFFFFF,
            override var rotation: Float = 0f,
            override val size: Float = 0.8f,
            var ledColor: Int = 0xFF0000) : GameObject() {
    val desiredVelocity: Vector2f = Vector2f(0f, 0f)

    var localTime: Float = 0f
    var scriptOrigin: Vector2fc = Vector2f()

    var hasDestination: Boolean = false
    val destination: Vector2f = Vector2f()
    var destinationTargetDistance: Float = 0f

    var miningBeam: LaserBeam? = null
    var selected: Boolean = false

    var carryingObject: CarryingObject? = null

    val scanQueue: Queue<ScanRequest> = LinkedList()
    val scanResultQueue: Queue<Vector2fc> = LinkedList()
    val detectedTiles: MutableSet<Vector2fc> = HashSet()

    val outgoingCommsQueue: Queue<CommsMessage> = LinkedList()
    val incomingCommsQueue: Queue<CommsMessage> = LinkedList()
    var isListeningForComms: Boolean = false

    val inventory = Inventory(10.0)

    val maxPower: Double = 10.0
    var currentPower: Double = maxPower
    var lastPowerConsumedTime: Float = 0.0f
    var shutdown: Float? = null

    var activeScanning: Boolean = false

    var scriptManager: ScriptManager? = null

    override val physics: PhysicsBehavior

    init {
        val body = Body(1)
        body.addFixture(Circle(size.toDouble() * 0.5))
        body.mass = Mass(Vector2(0.5, 0.5), 1.0, 1.0)
        body.transform.setTranslation(position.x.toDouble(), position.y.toDouble())
        physics = PhysicsBehavior(this, body, 0.0)
    }

    /**
     * Tries to consume the given amount of power. If there isn't enough power, consumes all that there is left. Then
     * returns the actual amount of power that was consumed.
     */
    fun consumePower(amount: Double): Double {
        // Update shutdown
        val s = shutdown
        if (s != null) {
            if (localTime - s < PowerConstants.SHUTDOWN_LENGTH) return 0.0
            else shutdown = null
        }

        // Consume power
        val amountConsumed = Math.min(amount, currentPower)
        currentPower -= amountConsumed

        if (amountConsumed > 0.0)
            lastPowerConsumedTime = this.localTime

        // Shut down if we are out of power
        if (currentPower == 0.0 && shutdown == null)
            shutdown = this.localTime

        return amountConsumed
    }
}
