package frame

import javafx.geometry.*
import javafx.scene.paint.*
import javafx.scene.text.*
import tornadofx.*


class Styles: Stylesheet() {
	
	companion object {
		val disablefocus by cssclass()
	}
	
	object Menubar {
		val itemshortcut by cssclass()
		val itemname by cssclass()
		val gridpane by cssclass()
	}
	
	object Tabs {
		val title by cssclass()
		val titlebuttons by cssclass()
		
		val maintab by cssclass()
		val seperator by cssclass()
		
		val topbar by cssclass()
		
		val shadowborder by cssclass()
	}
	
	object CalendarTableView {
		val table by cssclass()
		val tableitem by cssclass()
		
		val tablecell by cssclass()
		val disabledtablecell by cssclass()
		val hoveredtablecell by cssclass()
		val markedtablecell by cssclass()
		
		val tableheader by cssclass()
		val selectedcolumn by cssclass()
		
		val celllabel by cssclass()
		val cellheaderlabel by cssclass()
		val cellappointlabel by cssclass()
		val cellappointtypelabel by cssclass()
		
		val celllabelicon by cssclass()
	}
	
	object NoteTab {
		val texteditor by cssclass()
	}
	
	
	
	init {
		disablefocus {
			focusColor = Color.TRANSPARENT
		}
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
			maxWidth = 300.px
		}
	}
	
	init {
		Tabs.title {
			fontSize = 26.px
			fontWeight = FontWeight.BOLD
			textFill = Color.BLACK
		}
		
		Tabs.titlebuttons {
			focusColor = Color.BLACK
			fontWeight = FontWeight.BOLD
		}
		
		Tabs.maintab {
			borderColor += box(Color.TRANSPARENT)
			borderWidth += box(5.px)
			borderRadius += box(10.px)
		}
		
		Tabs.seperator {
			backgroundColor += Color.BLACK
			prefHeight = 2.px
			maxHeight = 2.px
			minHeight = 2.px
		}
		
		Tabs.topbar {
			minHeight = 50.px
			maxHeight = 50.px
			backgroundColor += Color.DODGERBLUE
		}
		
		Tabs.shadowborder {
			borderColor += box(Color.BLACK)
			borderWidth += box(5.px)
			borderRadius += box(10.px)
		}
	}
	
	init {
		CalendarTableView.table {
			prefHeight = Int.MAX_VALUE.px
			backgroundColor += Color.WHITE
			padding = box(3.px)
		}
		
		CalendarTableView.selectedcolumn {
			backgroundColor += c(0.94, 0.94, 0.94)
			backgroundRadius += box(6.px)
		}
		
		CalendarTableView.tableitem {
			alignment = Pos.CENTER
			prefWidth = Int.MAX_VALUE.px
			
			padding = box(2.px)
		}
		
		CalendarTableView.tableheader {
			prefHeight = 30.px
			minHeight = prefHeight
			
			borderColor += box(c(0.75, 0.75, 0.75))
			borderRadius += box(10.px, 10.px, 0.px, 0.px)
			borderWidth += box(2.px)
		}
		
		CalendarTableView.tablecell {
			prefHeight = 40.px
			
			backgroundColor += c(0.89, 0.89, 0.89)
			backgroundRadius += box(6.px)
			
			borderColor += box(c(0.75, 0.75, 0.75))
			borderRadius += box(6.px)
			borderWidth += box(2.px)
		}
		
		CalendarTableView.disabledtablecell {
			backgroundColor += c(0.98, 0.98, 0.98)
			backgroundRadius += box(6.px)
			borderWidth += box(2.px)
		}
		
		CalendarTableView.hoveredtablecell {
			borderColor += box(c(0, 151, 190), c(0, 136, 204), c(0, 151, 190), c(0, 136, 204))
			borderRadius += box(6.px)
			borderWidth += box(2.px)
		}
		
		CalendarTableView.markedtablecell {
			backgroundColor += Color.CORNFLOWERBLUE
			
			borderColor += box(Color.BLUE)
			borderRadius += box(6.px)
			borderWidth += box(2.px)
		}
		
		CalendarTableView.cellheaderlabel {
			fontSize = 13.px
			fontWeight = FontWeight.BOLD
		}
		
		CalendarTableView.celllabel {
			fontSize = 14.px
			fontWeight = FontWeight.BOLD
			
			prefWidth = Int.MAX_VALUE.px
			alignment = Pos.CENTER
		}
		
		CalendarTableView.celllabelicon {
			prefHeight = 14.px
			prefWidth = 14.px
		}
		
		CalendarTableView.cellappointlabel {
			fontSize = 10.px
		}
		
		CalendarTableView.cellappointtypelabel {
			fontSize = 12.px
		}
	}
	
	init {
		NoteTab.texteditor {
			maxHeight = 300.px
			minHeight = 140.px
		}
	}
}