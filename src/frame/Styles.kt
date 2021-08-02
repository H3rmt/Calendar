package frame

import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import tornadofx.Stylesheet
import tornadofx.box
import tornadofx.c
import tornadofx.cssclass
import tornadofx.px


class Styles: Stylesheet() {
	object Menubar {
		val itemshortcut by cssclass()
		val itemname by cssclass()
		val gridpane by cssclass()
	}

	object CalendarView {
		val title by cssclass()
		val column by cssclass()
		val table by cssclass()
		val tablecell by cssclass()
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
	}

	init {
		CalendarView.title {
			fontSize = 26.px
			fontWeight = FontWeight.BOLD
			textFill = Color.BLACK
		}

		CalendarView.table {
			focusColor = Color.TRANSPARENT
		}

		CalendarView.column {
			prefHeight = 45.px
		}

		CalendarView.tablecell {

		}
	}
}