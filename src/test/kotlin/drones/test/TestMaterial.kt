package drones.test

import drones.game.Inventory
import drones.game.Material
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TestMaterial {
    @Test
    fun testMaterialBasic() {
        val mat = Material("Test", 1.0, 0.001)
        val inv = Inventory(100.0)

        assertEquals(0.0, inv.getMass(mat))
        assertEquals(0.0, inv.getVolume(mat))
        assertEquals(emptyList<Material>(), inv.storedMaterials)

        inv.changeMaterial(mat, 10.0)

        assertEquals(10.0, inv.getMass(mat))
        assertEquals(10.0, inv.getVolume(mat))
        assertEquals(10.0, inv.currentVolume)
        assertEquals(listOf(mat), inv.storedMaterials)
    }

    @Test
    fun testMaterialEpsilon() {
        val mat = Material("TestEpsilon", 1.0, 2.0)
        val inv = Inventory(100.0)

        inv.changeMaterial(mat, 3.0)

        assertEquals(2.0, inv.getMass(mat))
    }

    @Test
    fun testMaterialDensity() {
        // Create material with 0.1kg/L density
        val mat = Material("TestDensity", 0.1, 0.001)
        val inv = Inventory(100.0)

        // Add 0.5kg of material
        inv.changeMaterial(mat, 0.5)

        assertEquals(0.5, inv.getMass(mat))
        assertEquals(5.0, inv.getVolume(mat))
    }

    @Test
    fun testMaterialMax() {
        val mat = Material("Test", 2.0, 0.001)
        val inv = Inventory(10.0)

        // Add 30kg of material -- equal to 15L volume
        inv.changeMaterial(mat, 30.0)

        // There should only be 20kg added
        assertEquals(20.0, inv.getMass(mat))
        assertEquals(10.0, inv.getVolume(mat))
        assertEquals(10.0, inv.currentVolume)
    }

    @Test
    fun testMaterialRemove() {
        val mat = Material("Test", 1.0, 0.001)
        val inv = Inventory(20.0)

        inv.changeMaterial(mat, 10.0)
        assertEquals(10.0, inv.getMass(mat))
        assertEquals(10.0, inv.currentVolume)
        assertEquals(listOf(mat), inv.storedMaterials)

        inv.changeMaterial(mat, 1.0)
        assertEquals(11.0, inv.getMass(mat))
        assertEquals(11.0, inv.currentVolume)
        assertEquals(listOf(mat), inv.storedMaterials)

        inv.changeMaterial(mat, -7.0)
        assertEquals(4.0, inv.getMass(mat))
        assertEquals(4.0, inv.currentVolume)

        inv.changeMaterial(mat, -5.0)
        assertEquals(0.0, inv.getMass(mat))
        assertEquals(0.0, inv.currentVolume)
        assertEquals(emptyList<Material>(), inv.storedMaterials)

        inv.changeMaterial(mat, 0.1)
        assertEquals(0.1, inv.getMass(mat))
        assertEquals(0.1, inv.currentVolume)
        assertEquals(listOf(mat), inv.storedMaterials)
    }
}
