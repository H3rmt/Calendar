package frame

import javafx.geometry.*
import javafx.scene.paint.*
import javafx.scene.text.*
import tornadofx.*


class Styles: Stylesheet() {
	object Menubar {
		val itemshortcut by cssclass()
		val itemname by cssclass()
		val gridpane by cssclass()
	}
	
	object CalendarView {
		val title by cssclass()
		val titlebuttons by cssclass()
		
		val table by cssclass()
		val tableitem by cssclass()
		
		val tablecell by cssclass()
		val hoveredtablecell by cssclass()
		
		val tableheader by cssclass()
		val selectedcolumn by cssclass()
		
		val celllabel by cssclass()
		val cellheaderlabel by cssclass()
		val cellappointlabel by cssclass()
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
		
		CalendarView.titlebuttons {
			focusColor = Color.BLACK
			fontWeight = FontWeight.BOLD
		}
		
		CalendarView.table {
			prefHeight = Int.MAX_VALUE.px
		}
		
		CalendarView.tableitem {
			alignment = Pos.CENTER
			prefWidth = Int.MAX_VALUE.px
			
			padding = box(2.px)
		}
		
		CalendarView.tableheader {
			prefHeight = 30.px
			minHeight = 25.px
			
			borderColor += box(Color.LIGHTGRAY)
			borderWidth += box(1.px)
			borderRadius += box(10.px, 10.px, 0.px, 0.px)
		}
		
		CalendarView.selectedcolumn {
			backgroundColor += c(0.95, 0.95, 0.95)
		}
		
		CalendarView.tablecell {
			prefHeight = 45.px
			
			borderColor += box(c(0.82, 0.82, 0.82, 0.4))
			borderRadius += box(6.px)
			borderWidth += box(2.px)
		}
		
		CalendarView.hoveredtablecell {
			borderColor += box(c(0, 151, 190), c(0, 136, 204), c(0, 151, 190), c(0, 136, 204))
			borderRadius += box(6.px)
			borderWidth += box(2.px)
		}
		
		CalendarView.cellheaderlabel {
			fontSize = 12.px
			fontWeight = FontWeight.BOLD
		}
		
		CalendarView.celllabel {
			fontSize = 14.px
			fontWeight = FontWeight.BOLD
			
			padding = box(3.px, 0.px, 0.px, 0.px)
		}
		
		CalendarView.cellappointlabel {
			fontSize = 10.px
		}
	}
}