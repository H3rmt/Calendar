package frame

import javafx.geometry.Pos
import javafx.scene.text.FontWeight
import tornadofx.*
import javafx.scene.paint.Color
import javafx.scene.paint.Paint


class Styles : Stylesheet() {
	companion object {
		val menubaritemshortcut by cssclass()
		val menubaritemname by cssclass()
		val menubaritem by cssclass()
		val menubaritembox by cssclass()
	}
	init {
		menubaritemshortcut {
			fontSize = 9.px
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
			backgroundColor += Color.RED
		}

		menubaritembox {
			backgroundColor += Color.AQUA
			prefWidth = 100.px
			alignment = Pos.TOP_RIGHT
		}
	}
}