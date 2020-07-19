package drones.game

import drones.*
import org.dyn4j.dynamics.RaycastResult
import org.joml.Vector2ic

private const val MINING_LASER_RAYCAST_INTERVAL = 0.25f
private const val MINING_LASER_ORE_PER_SECOND = 1.0

class MiningLaserBehavior(private val state: GameState, private val laser: LaserBeam,
                          private val depositToInventory: Inventory) : Behavior {
    private var timeUntilNextRaycast: Float = 0f
    private var timeUntilTileBreak: Float = 2f

    override fun update(deltaTime: Float) {
        timeUntilNextRaycast -= deltaTime
        timeUntilTileBreak -= deltaTime

        if (timeUntilNextRaycast <= 0) {
            timeUntilNextRaycast  = MINING_LASER_RAYCAST_INTERVAL

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

            val hit = raycastResult.getOrNull(0)
            if (hit?.body == state.gridBody) {
                val hitGridCoordinates = hit.fixture.userData as Vector2ic
                val hitTile = state.grid.tiles[hitGridCoordinates.y()][hitGridCoordinates.x()]

                if (hitTile == TileStone)
                    depositToInventory.changeMaterial(Materials.ORE,
                        MINING_LASER_ORE_PER_SECOND * MINING_LASER_RAYCAST_INTERVAL)

                if (timeUntilTileBreak <= 0f) {
                    state.grid.tiles[hitGridCoordinates.y()][hitGridCoordinates.x()] = TileAir
                    hit.body.removeFixture(hit.fixture)
                    timeUntilTileBreak = 2f
                }
            }
        }
    }
}

class CreateMiningLaserBehavior(private val laser: LaserBeam,
                                private val depositToInventory: Inventory) : CreateBehavior {
    override fun create(state: GameState): Behavior =
        MiningLaserBehavior(state, laser, depositToInventory)
}
