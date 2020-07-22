package drones.game

import org.dyn4j.dynamics.Body
import org.dyn4j.geometry.Geometry
import org.dyn4j.geometry.MassType
import org.joml.Vector2f
import org.joml.Vector2fc

class Base(override val position: Vector2f) : GameObject() {
    override val rotation: Float = 0f
    override val size = 2.0f

    override val physics: PhysicsBehavior

    val inventory = Inventory(1000.0)

    init {
        createBehavior = CreateBaseBehavior(this)

        val body = Body(1)
        body.addFixture(Geometry.createRectangle(size.toDouble(), size.toDouble()))
        body.setMass(MassType.INFINITE)
        body.transform.setTranslation(position.x().toDouble(), position.y().toDouble())
        physics = PhysicsBehavior(this, body)
    }
}
