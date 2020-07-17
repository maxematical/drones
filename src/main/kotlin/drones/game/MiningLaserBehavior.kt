package drones.game

import drones.*
import org.dyn4j.dynamics.RaycastResult
import org.joml.Vector2ic

class MiningLaserBehavior(private val state: GameState, private val laser: LaserBeam) : Behavior {
    override fun update(deltaTime: Float) {
        laser.lifetime += deltaTime

        val rotationRad = (laser.rotation * MathUtils.DEG2RAD).toDouble()
        val laserStart = laser.position.toDyn4j()
        val laserEnd = laserStart.copy().add(Math.cos(rotationRad) * laser.unobstructedLength,
            Math.sin(rotationRad) * laser.unobstructedLength)

        val raycastResult = mutableListOf<RaycastResult>()
        if (state.world.raycast(laserStart, laserEnd, { true }, true, false, false, raycastResult)) {
            laser.actualLength = raycastResult[0].raycast.distance.toFloat()
        } else {
            laser.actualLength = laser.unobstructedLength
        }

        if (laser.lifetime >= 2.0f) {
            laser.lifetime = 0f

            if (raycastResult.isNotEmpty()) {
                val hit = raycastResult[0]
                if (hit.body == state.gridBody) {
                    val hitGridCoordinates = hit.fixture.userData as Vector2ic
                    state.grid.tiles[hitGridCoordinates.y()][hitGridCoordinates.x()] = TileAir
                    hit.body.removeFixture(hit.fixture)
                }
            }
        }
    }
}

class CreateMiningLaserBehavior(private val laser: LaserBeam) : CreateBehavior {
    override fun create(state: GameState): Behavior =
        MiningLaserBehavior(state, laser)
}
