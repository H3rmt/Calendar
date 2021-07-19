package frame

import javafx.geometry.Pos
import javafx.scene.text.FontWeight
import tornadofx.*
import javafx.scene.paint.Color


class Styles : Stylesheet() {
	companion object {
		val menubaritemshortcut by cssclass()
		val menubaritemname by cssclass()
		val menubaritem by cssclass()
	}
	init {
		menubaritemshortcut {
			fontSize = 10.px
			textFill = Color.GRAY
			fontWeight = FontWeight.THIN
			backgroundColor += Color.AZURE
		}

		menubaritemname {
			fontSize = 12.px
			textFill = Color.BLACK
			fontWeight = FontWeight.NORMAL
			backgroundColor += Color.BEIGE
		}

		menubaritem {
			spacing = 10.px
			alignment = Pos.CENTER_RIGHT
			backgroundColor += Color.RED
		}
	}
}