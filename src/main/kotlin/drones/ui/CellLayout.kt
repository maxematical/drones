package drones.ui

import org.joml.Vector2f
import org.joml.Vector2fc

class CellLayout(private val child: Ui, private val parent: Ui) : Layout {
    private val childIndex: Int = parent.children.indexOf(child)
    private val row = 0 // TODO
    private val col = 0 // TODO

    private val dimensionsVec = Vector2f()
    private val anchorPointVec = Vector2f()
    private val positionVec = Vector2f()

    override val dimensions: Vector2fc = dimensionsVec
    override val anchorPoint: Vector2fc = anchorPointVec
    override val position: Vector2fc = positionVec

    override fun updateLayout() {

    }
}
