package drones.game

import org.dyn4j.dynamics.Body
import org.dyn4j.geometry.Geometry
import org.dyn4j.geometry.Mass
import org.dyn4j.geometry.Vector2
import org.joml.Vector2f

class OreChunk(override val position: Vector2f, override val rotation: Float) : GameObject() {
    override val physics: PhysicsBehavior
    override val size = 0.8f

    init {
        val body = Body(1)
        body.addFixture(Geometry.createCircle(size * 0.5))
        body.mass = Mass(Vector2(0.0, 0.0), 2.5, 1.0)
        body.transform.setTranslation(position.x.toDouble(), position.y.toDouble())
        physics = PhysicsBehavior(this, body)
    }
}
