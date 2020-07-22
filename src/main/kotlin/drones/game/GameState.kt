package drones.game

import org.dyn4j.dynamics.Body
import org.dyn4j.dynamics.World
import org.joml.Vector2fc
import java.util.*

class GameState(
    val world: World,
    val grid: Grid,
    val gridBody: Body,
    val spawnQueue: Queue<GameObject>,
    val despawnQueue: Queue<GameObject>,
    val objects: List<GameObject>
) {
    fun getObjectsInRange(point: Vector2fc, radius: Float, out: MutableList<GameObject>) {
        for (obj in objects) {
            if (obj.position.distanceSquared(point) <= radius * radius) {
                out.add(obj)
            }
        }
    }
}
