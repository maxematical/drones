package drones.game

import org.dyn4j.dynamics.Body

data class CarryingObject(
    val carrying: GameObject,
    val carryingBody: Body
)
