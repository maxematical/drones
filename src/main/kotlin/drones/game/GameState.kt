package drones.game

import org.dyn4j.dynamics.Body
import org.dyn4j.dynamics.World
import java.util.*

class GameState(
    val world: World,
    val grid: Grid,
    val gridBody: Body,
    val spawnQueue: Queue<GameObject>,
    val despawnQueue: Queue<GameObject>,
    val objects: List<GameObject>
)
