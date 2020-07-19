package drones.game

import drones.MathUtils
import kotlin.math.floor

class Inventory(val capacity: Double) {
    private val stacks = mutableMapOf<Material, Double>()

    private var mutStoredMaterials = mutableListOf<Material>()
    val storedMaterials: List<Material> = mutStoredMaterials

    var currentVolume: Double = 0.0
        private set

    fun getMass(material: Material): Double =
        stacks.getOrDefault(material, 0.0)

    fun getVolume(material: Material): Double =
        getMass(material) / material.density

    /**
     * Adds or removes the given mass of material to this inventory.
     *
     * @param material the material in question
     * @param amount how many kg to add or remove
     */
    fun changeMaterial(material: Material, amount: Double) {
        val currentAmount = getMass(material)

        // Calculate how much mass of material we can add without going over (or under) volume restrictions
        val volumeLeft = capacity - currentVolume
        val massLeft = volumeLeft * material.density
        val addMass = MathUtils.clamp(-currentAmount, massLeft, material.fixAmount(amount))
        val nextAmount = currentAmount + addMass

        // Update whether this material is stored in the inventory
        if (currentAmount == 0.0 && nextAmount > 0.0) {
            mutStoredMaterials.add(material)
        }
        if (currentAmount > 0.0 && nextAmount == 0.0) {
            mutStoredMaterials.remove(material)
        }

        // Update stored mass for this material in stacks, and recalculate current volume
        stacks[material] = nextAmount
        currentVolume += addMass / material.density
    }
}

class Material(
    val name: String,
    val density: Double,
    val amountEpsilon: Double
) {
    fun fixAmount(amount: Double): Double =
        floor(amount / amountEpsilon) * amountEpsilon
}

object Materials {
    val ORE = Material("Ore", 1.0, 0.001)
}
