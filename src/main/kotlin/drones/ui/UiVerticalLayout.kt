package drones.ui

class UiVerticalLayout : UiLayout() {
    private val mutableChildren = mutableListOf<UiElement>()
    override val children: List<UiElement> = mutableChildren

    fun addChild(element: UiElement) {
        mutableChildren.add(element)
    }

    override fun computeChildProportions(parent: UiElement) {
        var nextY = 0f
        for (child in children) {
            child.computedDimensions.set(child.autoDimensions)
            child.computedPosition.set(0f, nextY)
            nextY += child.computedDimensions.y()
        }
    }
}
