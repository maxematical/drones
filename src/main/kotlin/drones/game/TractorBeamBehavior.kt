package drones.game

import drones.MathUtils
import org.dyn4j.dynamics.Body
import org.dyn4j.dynamics.joint.RopeJoint
import org.dyn4j.geometry.Vector2
import org.joml.Vector2fc

class TractorBeamBehavior(private val gameState: GameState, private val beam: LaserBeam,
                          private val ownerPosition: Vector2fc, private val targetPosition: Vector2fc,
                          private val ownerBody: Body, private val targetBody: Body) :
        EntityBehavior {
    private var constraint: RopeJoint? = null

    private val ownerPhysicsPosition = Vector2()
    private val targetPhysicsPosition = Vector2()

    override fun update(deltaTime: Float) {
        ownerPhysicsPosition.set(ownerPosition.x().toDouble(), ownerPosition.y().toDouble())
        targetPhysicsPosition.set(targetPosition.x().toDouble(), targetPosition.y().toDouble())

        if (constraint == null) {
            val joint = RopeJoint(ownerBody, targetBody, ownerPhysicsPosition, targetPhysicsPosition)
            joint.lowerLimit = 0.75
            joint.upperLimit = 2.5
            gameState.world.addJoint(joint)
            constraint = joint
        }

        beam.rotation = Math.atan2(targetPosition.y() - ownerPosition.y().toDouble(),
            targetPosition.x() - ownerPosition.x().toDouble()).toFloat() * MathUtils.RAD2DEG
        beam.unobstructedLength = ownerPosition.distance(targetPosition)
        beam.actualLength = ownerPosition.distance(targetPosition)
    }

    override fun remove() {
        constraint?.let(gameState.world::removeJoint)
    }
}
