package drones.game

import drones.scripting.ScriptManager
import org.dyn4j.dynamics.Body
import org.dyn4j.geometry.Circle
import org.dyn4j.geometry.Mass
import org.dyn4j.geometry.Vector2
import org.joml.Vector2f
import org.joml.Vector2fc
import java.util.*
import kotlin.properties.Delegates
import kotlin.properties.Delegates.observable

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

    val inventory = Inventory(10.0)

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
}
