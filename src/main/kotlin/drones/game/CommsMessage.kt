package drones.game

import org.luaj.vm2.LuaValue

class CommsMessage(
    val from: Drone,
    val message: String,
    val contents: LuaValue
)
