package drones.game

class BaseBehavior(private val base: Base, private val state: GameState) : Behavior {
    private val nearbyObjectsList = mutableListOf<GameObject>()

    override fun update(deltaTime: Float) {
        // Find nearby inventories
        nearbyObjectsList.clear()
        state.getObjectsInRange(base.position, 3f, nearbyObjectsList)

        // Transfer inventory contents into the base
        for (nearbyObject in nearbyObjectsList) {
            if (nearbyObject is Drone) {
                if (nearbyObject.inventory.isEmpty)
                    continue

                val material = nearbyObject.inventory.storedMaterials[0]
                nearbyObject.inventory.changeMaterial(material, -TRANSFER_LITERS_PER_SECOND * deltaTime)
                base.inventory.changeMaterial(material, TRANSFER_LITERS_PER_SECOND * deltaTime)
            }
        }
    }

    companion object {
        const val TRANSFER_LITERS_PER_SECOND: Double = 2.5
    }
}

class CreateBaseBehavior(private val base: Base) : CreateBehavior {
    override fun create(state: GameState): Behavior =
        BaseBehavior(base, state)
}
