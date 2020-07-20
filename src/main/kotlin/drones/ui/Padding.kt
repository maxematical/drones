package drones.ui

class Padding(
    var top: Float,
    var left: Float,
    var bottom: Float,
    var right: Float
) {
    val totalVertical get() = top + bottom
    val totalHorizontal get() = left + right

    constructor(amount: Float) : this(amount, amount)
    constructor(vertical: Float, horizontal: Float) : this(vertical, horizontal, vertical, horizontal)

    fun set(top: Float, left: Float, bottom: Float, right: Float) {
        this.top = top
        this.left = left
        this.bottom = bottom
        this.right = right
    }

    fun set(vertical: Float, horizontal: Float) {
        set(vertical, horizontal, vertical, horizontal)
    }

    fun set(amount: Float) {
        set(amount, amount)
    }
}
