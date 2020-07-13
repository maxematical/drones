package drones

import org.joml.Vector2fc

class LaserBeam(override val position: Vector2fc,
                override var rotation: Float,
                var width: Float,
                var unobstructedLength: Float) : GameObject() {

    var actualLength: Float = unobstructedLength

    var lifetime: Float = 0f
}
