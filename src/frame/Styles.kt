package frame

import javafx.scene.text.FontWeight
import tornadofx.*
import javafx.scene.paint.Color


class Styles : Stylesheet() {
	object Menubar {
		val itemshortcut by cssclass()
		val itemname by cssclass()
		val gridpane by cssclass()
	}

	object CalendarView {
		val title by cssclass()
	}

	init {
		Menubar.itemshortcut {
			fontSize = 9.px
			fontWeight = FontWeight.THIN
			textFill = Color.DARKGRAY
		}

		Menubar.itemname {
			fontSize = 12.px
			fontWeight = FontWeight.NORMAL
			textFill = Color.BLACK
		}

		Menubar.gridpane {
			prefWidth = 0.px
			maxWidth = 300.px
		}

		CalendarView.title {
			fontSize = 26.px
			fontWeight = FontWeight.BOLD
			textFill = Color.BLACK
		}
	}
}