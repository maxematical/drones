package drones.game

import drones.MathUtils
import drones.toDyn4j
import org.dyn4j.dynamics.RaycastResult
import org.joml.Vector2ic

private const val MINING_LASER_RAYCAST_INTERVAL = 0.25f
private const val MINING_LASER_ORE_PER_SECOND = 1.0

class MiningLaserBehavior(private val state: GameState, private val laser: LaserBeam,
                          private val depositToInventory: Inventory) : Behavior {
    private var timeUntilNextRaycast: Float = 0f
    private var timeUntilTileBreak: Float = 2f

    private var lastLength = laser.actualLength
    private var nextLength = laser.actualLength

    private var didRaycastYet = false

    override fun update(deltaTime: Float) {
        // Update timers
        timeUntilNextRaycast -= deltaTime
        timeUntilTileBreak -= deltaTime

        // Interpolate length
        if (didRaycastYet) {
            val timeSinceLastRaycast = MINING_LASER_RAYCAST_INTERVAL - timeUntilNextRaycast
            laser.actualLength = MathUtils.lerp(lastLength, nextLength,
                timeSinceLastRaycast / MINING_LASER_RAYCAST_INTERVAL)
        }

        // Perform raycast if necessary
        if (timeUntilNextRaycast <= 0) {
            timeUntilNextRaycast = MINING_LASER_RAYCAST_INTERVAL

            // Compute raycast parameters
            val rotationRad = (laser.rotation * MathUtils.DEG2RAD).toDouble()
            val laserStart = laser.position.toDyn4j()
            val laserEnd = laserStart.copy().add(Math.cos(rotationRad) * laser.unobstructedLength,
                Math.sin(rotationRad) * laser.unobstructedLength)

            // Do raycast and update laser length
            lastLength = nextLength
            val raycastResult = mutableListOf<RaycastResult>()
            if (state.world.raycast(laserStart, laserEnd, { true }, true, false, false, raycastResult)) {
                nextLength = raycastResult[0].raycast.distance.toFloat()
            } else {
                nextLength = laser.unobstructedLength
            }
            if (!didRaycastYet)
                lastLength = nextLength

            // Check if we hit an ore tile
            val hit = raycastResult.getOrNull(0)
            if (hit?.body == state.gridBody) {
                val hitGridCoordinates = hit.fixture.userData as Vector2ic
                val hitX = hitGridCoordinates.x()
                val hitY = hitGridCoordinates.y()
                val hitTile = state.grid.getTile(hitX, hitY)
                val hitMeta = state.grid.getMeta(hitX, hitY)

                // Remove ore from world and deposit into inventory
                if (hitTile == TileOre) {
                    val (newData, kgExtracted) = TileOre.mineTile(hitMeta)
                    depositToInventory.changeMaterial(Materials.ORE, kgExtracted)
                    state.grid.setData(hitX, hitY, newData)
                }
            }

            // Mark that we performed a raycast
            didRaycastYet = true
        }
    }
}

class CreateMiningLaserBehavior(private val laser: LaserBeam,
                                private val depositToInventory: Inventory) : CreateBehavior {
    override fun create(state: GameState): Behavior =
        MiningLaserBehavior(state, laser, depositToInventory)
}
