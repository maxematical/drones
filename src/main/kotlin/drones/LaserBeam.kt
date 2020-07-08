package drones

import org.joml.Vector2f
import org.joml.Vector2fc

class LaserBeam(val position: Vector2fc,
                var rotation: Float,
                var width: Float,
                var length: Float) {

    var renderer: LaserBeamRenderer? = null

}
