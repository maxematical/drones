package drones

import org.joml.Vector2f

class LaserBeam(val position: Vector2f,
                var rotation: Float,
                var width: Float,
                var length: Float) {

    var renderer: LaserBeamRenderer? = null

}
