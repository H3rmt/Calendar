package frame

import javafx.scene.text.FontWeight
import tornadofx.*
import javafx.scene.paint.Color


class Styles : Stylesheet() {
	companion object {
		val header by cssclass()
	}
	init {
		header {
			fontSize = 20.px
			fontWeight = FontWeight.BOLD
			backgroundColor += Color.ORANGE
		}
	}
}