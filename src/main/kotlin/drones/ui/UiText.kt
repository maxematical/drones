package drones.ui

class UiText(params: Params) : Ui(params) {
    var requestedString: String? = null
    var textBgColor: Int = 0
    var textFgColor: Int = 15
    var transparentTextBg: Boolean = false
    var textAlign: TextAlign = TextAlign.LEFT
    var fontScale: Float = 2f
    var fontSpacing: Float = 12f
}
