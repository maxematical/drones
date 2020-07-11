package drones

import org.joml.Vector2f

class PausedReminder(screenDimensions: Vector2f) : Ui(screenDimensions) {
    override fun updateDimensions() {
        dimensionsVec.set(/*180f*/1000f, 30f)
    }

    override fun updatePosition() {
        positionVec.set(screenWidth * 0.5f, 40f)
    }

    override fun updateAnchorPoint() {
        anchorPointVec.set(0f, -1f)
    }
}
