package drones.game

import org.dyn4j.dynamics.Body
import org.dyn4j.geometry.Circle
import org.dyn4j.geometry.Mass
import org.dyn4j.geometry.Vector2
import org.joml.Vector2f
import org.joml.Vector2fc

class Drone(val grid: Grid,
            override val position: Vector2f,
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
    var carryingObject: GameObject? = null

    override val physicsBody: Body
    var physicalMass: Double = 1.0

    init {
        physicsBody = Body(1)
        physicsBody.addFixture(Circle(size.toDouble() * 0.5))
        physicsBody.mass = Mass(Vector2(0.5, 0.5), physicalMass, 1.0)
        physicsBody.transform.setTranslation(position.x.toDouble(), position.y.toDouble())
    }
}
