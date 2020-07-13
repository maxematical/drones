package drones

import org.dyn4j.dynamics.Body
import org.joml.Matrix4f
import org.joml.Matrix4fc
import org.joml.Vector2fc

abstract class GameObject {
    abstract val position: Vector2fc
    abstract val rotation: Float
    open val size: Float = 1.0f
    open val physicsBody: Body? = null

    var spawned: Boolean = false
    var requestDespawn: Boolean = false

    var behavior: EntityBehavior = IdleBehavior
    var renderer: Renderer = DummyRenderer
    var hoverable: Hoverable = DummyHoverable

    private val mutModelMatrix: Matrix4f = Matrix4f()
    private val mutModelMatrixInv: Matrix4f = Matrix4f()

    val modelMatrix: Matrix4fc = mutModelMatrix
    val modelMatrixInv: Matrix4fc = mutModelMatrixInv
    val modelMatrixArr: FloatArray = FloatArray(16)

    init {
        //recomputeModelMatrix()
    }

    fun recomputeModelMatrix() {
        mutModelMatrix.identity()
        mutModelMatrix.translate(position.x(), position.y(), 0f)
        mutModelMatrix.rotate(rotation * MathUtils.DEG2RAD, 0f, 0f, 1f)
        mutModelMatrix.scale(size)
        mutModelMatrix.invert(mutModelMatrixInv)
        mutModelMatrix.get(modelMatrixArr)
    }
}
