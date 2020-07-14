package drones.ui

abstract class UiLayout {
    abstract val children: List<UiElement>

    abstract fun computeChildProportions(parent: UiElement)
}
