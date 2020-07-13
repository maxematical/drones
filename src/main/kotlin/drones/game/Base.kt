package drones.game

import org.dyn4j.dynamics.Body
import org.dyn4j.geometry.Geometry
import org.dyn4j.geometry.MassType
import org.joml.Vector2f
import org.joml.Vector2fc

class Base(override val position: Vector2f) : GameObject() {
    override val rotation: Float = 0f
    override val physicsBody: Body
    override val size = 2.0f

    init {
        physicsBody = Body(1)
        physicsBody.addFixture(Geometry.createRectangle(size.toDouble(), size.toDouble()))
        physicsBody.setMass(MassType.INFINITE)
        physicsBody.transform.setTranslation(position.x().toDouble(), position.y().toDouble())
    }
}
