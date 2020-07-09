package drones

import org.joml.Vector2fc

class LaserBeam(val position: Vector2fc,
                var rotation: Float,
                var width: Float,
                var unobstructedLength: Float) {

    var actualLength: Float = unobstructedLength

    var renderer: Renderer? = null
    var lifetime: Float = 0f

}
