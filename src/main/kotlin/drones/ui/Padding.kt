package drones.ui

class Padding(
    var top: Float,
    var left: Float,
    var bottom: Float,
    var right: Float
) {
    val totalHorizontal get() = left + right
    val totalVertical get() = top + bottom

    constructor(amount: Float) : this(amount, amount)
    constructor(vertical: Float, horizontal: Float) : this(vertical, horizontal, vertical, horizontal)
}
