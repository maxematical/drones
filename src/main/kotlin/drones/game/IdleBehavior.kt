package drones.game

object IdleBehavior : Behavior {
    override fun update(deltaTime: Float) {}
}

object CreateIdleBehavior : CreateBehavior {
    override fun create(state: GameState): Behavior = IdleBehavior
}
