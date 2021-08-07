package frame

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
		val column by cssclass()
		val table by cssclass()
		val disabledtablecell by cssclass()
		val tablecell by cssclass()
		val hoveredtablecell by cssclass()
		val tablecellpane by cssclass()
		val hoveredtablecellpane by cssclass()
		
		val celllabel by cssclass()
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
		}
		
		CalendarView.table {
			focusColor = Color.TRANSPARENT
			
			selected {
				backgroundColor += c(232, 232, 232)
			}
			
			scrollBar {
				prefWidth = 0.px
				prefHeight = 0.px
				
				decrementArrow {
					prefWidth = 0.px
					prefHeight = 0.px
				}
				incrementArrow {
					prefWidth = 0.px
					prefHeight = 0.px
				}
			}
		}
		
		CalendarView.column {
			prefHeight = 40.px
		}
		
		CalendarView.disabledtablecell {
			prefHeight = 45.px
			borderColor += box(Color.ORANGE)
			borderWidth += box(0.px, 0.px, 0.px, 0.px)
		}
		
		CalendarView.tablecell {
			prefHeight = 46.px
			backgroundColor += Color.WHITE
			padding = box(2.px, 2.px, 1.px, 2.px)
			
			borderColor += box(Color.TRANSPARENT)
		}
		
		CalendarView.hoveredtablecell {
			//prefHeight = 66.px // Done by timeline animation
			//backgroundColor += c(245, 245, 245, .4)
		}
		
		CalendarView.tablecellpane {
			//padding = box(10.px)
			
			borderColor += box(Color.LIGHTGRAY)
			borderWidth += box(1.px)
			borderRadius += box(6.px)
		}
		
		CalendarView.hoveredtablecellpane {
			borderColor += box(c(0, 151, 190), c(0, 136, 204), c(0, 151, 190), c(0, 136, 204))
			borderRadius += box(6.px)
			borderWidth += box(2.px)
		}
		
		CalendarView.celllabel {
			fontSize = 14.px
			fontWeight = FontWeight.BOLD
			textFill = Color.BLACK
			
			padding = box(4.px, 0.px, 0.px, 0.px)
		}
		
	}
}