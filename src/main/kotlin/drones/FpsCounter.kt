package drones

import org.joml.Vector2f

class FpsCounter(screenDimensions: Vector2f) : Ui(screenDimensions) {
    override fun updateDimensions() {
        dimensionsVec.set(120f, 38f)
    }

    override fun updatePosition() {
        positionVec.set(screenWidth - 14f, screenHeight - 10f)
    }

    override fun updateAnchorPoint() {
        anchorPointVec.set(1f, 1f)
    }
}
