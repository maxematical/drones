package drones.game

import drones.MathUtils
import org.dyn4j.dynamics.Body
import org.dyn4j.dynamics.Force
import org.dyn4j.dynamics.joint.Joint
import org.dyn4j.dynamics.joint.RopeJoint
import org.dyn4j.dynamics.joint.WeldJoint
import org.dyn4j.geometry.Vector2

class TractorBeamBehavior(private val gameState: GameState, private val beam: LaserBeam,
                          private val ownerBody: Body, private val targetBody: Body) :
        Behavior {
    private var constraint: Joint? = null

    private val ownerPosition = Vector2()
    private val targetPosition = Vector2()

    private val desiredTargetVelocity = Vector2()
    private val targetForceVec = Vector2()
    private val targetForce = Force()

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

        val distance = desiredTargetVelocity.set(ownerPosition).subtract(targetPosition).normalize()
        val desiredSpeed = org.joml.Math.clamp(0.0, 1.5, (distance - 1.5) / 3.0)
        desiredTargetVelocity.multiply(desiredSpeed)

        targetForceVec.set(desiredTargetVelocity).subtract(targetBody.linearVelocity).multiply(0.2)
        targetForce.set(targetForceVec)
        targetBody.applyForce(targetForce)

        beam.rotation = Math.atan2(targetPosition.y - ownerPosition.y, targetPosition.x - ownerPosition.x)
            .toFloat() * MathUtils.RAD2DEG
        beam.unobstructedLength = ownerPosition.distance(targetPosition).toFloat()
        beam.actualLength = beam.unobstructedLength
    }

    override fun destroy() {
        constraint?.let(gameState.world::removeJoint)
    }
}
