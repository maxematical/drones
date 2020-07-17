package drones.game

import drones.MathUtils
import org.dyn4j.dynamics.Body
import org.dyn4j.dynamics.joint.RopeJoint
import org.dyn4j.geometry.Vector2
import org.joml.Vector2fc

class TractorBeamBehavior(private val gameState: GameState, private val beam: LaserBeam,
                          private val ownerBody: Body, private val targetBody: Body) :
        Behavior {
    private var constraint: RopeJoint? = null

    private val ownerPosition = Vector2()
    private val targetPosition = Vector2()

    override fun update(deltaTime: Float) {
        ownerPosition.set(ownerBody.transform.translationX, ownerBody.transform.translationY)
        targetPosition.set(targetBody.transform.translationX, targetBody.transform.translationY)

        if (constraint == null) {
            val joint = RopeJoint(ownerBody, targetBody, ownerPosition, targetPosition)
            joint.lowerLimit = 0.75
            joint.upperLimit = 2.5
            gameState.world.addJoint(joint)
            constraint = joint
        }

        beam.rotation = Math.atan2(targetPosition.y - ownerPosition.y, targetPosition.x - ownerPosition.x)
            .toFloat() * MathUtils.RAD2DEG
        beam.unobstructedLength = ownerPosition.distance(targetPosition).toFloat()
        beam.actualLength = beam.unobstructedLength
    }

    override fun destroy() {
        constraint?.let(gameState.world::removeJoint)
    }
}
