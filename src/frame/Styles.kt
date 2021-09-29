package frame

import javafx.geometry.*
import javafx.scene.paint.*
import javafx.scene.text.*
import tornadofx.*


class Styles: Stylesheet() {
	
	companion object {
		val disableFocus by cssclass()
	}
	
	object Menubar {
		val itemShortcut by cssclass()
		val itemName by cssclass()
		val gridPane by cssclass()
	}
	
	object Tabs {
		val title by cssclass()
		val titleButtons by cssclass()
		
		val mainTab by cssclass()
		val separator by cssclass()
		
		val topbar by cssclass()
		
		val shadowBorder by cssclass()
	}
	
	object CalendarTableView {
		val table by cssclass()
		val tableItem by cssclass()
		
		val tableCell by cssclass()
		val disabledTableCell by cssclass()
		val hoveredTableCell by cssclass()
		val markedTableCell by cssclass()
		
		val tableHeader by cssclass()
		val selectedColumn by cssclass()
		
		val cellLabel by cssclass()
		val cellHeaderLabel by cssclass()
		val cellAppointLabel by cssclass()
		val cellAppointTypeLabel by cssclass()
		
		val cellLabelIcon by cssclass()
	}
	
	object NoteTab {
		val texteditor by cssclass()
	}
	
	
	
	init {
		disableFocus {
			focusColor = Color.TRANSPARENT
		}
	}
	
	init {
		Menubar.itemShortcut {
			fontSize = 9.px
			fontWeight = FontWeight.THIN
			textFill = Color.DARKGRAY
		}
		
		Menubar.itemName {
			fontSize = 12.px
			fontWeight = FontWeight.NORMAL
			textFill = Color.BLACK
		}
		
		Menubar.gridPane {
			maxWidth = 300.px
		}
	}
	
	init {
		Tabs.title {
			fontSize = 26.px
			fontWeight = FontWeight.BOLD
			textFill = Color.BLACK
		}
		
		Tabs.titleButtons {
			focusColor = Color.BLACK
			fontWeight = FontWeight.BOLD
		}
		
		Tabs.mainTab {
			borderColor += box(Color.TRANSPARENT)
			borderWidth += box(5.px)
			borderRadius += box(10.px)
		}
		
		Tabs.separator {
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
		
		Tabs.shadowBorder {
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
		
		CalendarTableView.selectedColumn {
			backgroundColor += c(0.94, 0.94, 0.94)
			backgroundRadius += box(6.px)
		}
		
		CalendarTableView.tableItem {
			alignment = Pos.CENTER
			prefWidth = Int.MAX_VALUE.px
			
			padding = box(2.px)
		}
		
		CalendarTableView.tableHeader {
			prefHeight = 30.px
			minHeight = prefHeight
			
			borderColor += box(c(0.75, 0.75, 0.75))
			borderRadius += box(10.px, 10.px, 0.px, 0.px)
			borderWidth += box(2.px)
		}
		
		CalendarTableView.tableCell {
			prefHeight = 40.px
			
			backgroundColor += c(0.89, 0.89, 0.89)
			backgroundRadius += box(6.px)
			
			borderColor += box(c(0.75, 0.75, 0.75))
			borderRadius += box(6.px)
			borderWidth += box(2.px)
		}
		
		CalendarTableView.disabledTableCell {
			backgroundColor += c(0.98, 0.98, 0.98)
			backgroundRadius += box(6.px)
			borderWidth += box(2.px)
		}
		
		CalendarTableView.hoveredTableCell {
			borderColor += box(c(0, 151, 190), c(0, 136, 204), c(0, 151, 190), c(0, 136, 204))
			borderRadius += box(6.px)
			borderWidth += box(2.px)
		}
		
		CalendarTableView.markedTableCell {
			backgroundColor += Color.CORNFLOWERBLUE
			
			borderColor += box(Color.BLUE)
			borderRadius += box(6.px)
			borderWidth += box(2.px)
		}
		
		CalendarTableView.cellHeaderLabel {
			fontSize = 13.px
			fontWeight = FontWeight.BOLD
		}
		
		CalendarTableView.cellLabel {
			fontSize = 14.px
			fontWeight = FontWeight.BOLD
			
			prefWidth = Int.MAX_VALUE.px
			alignment = Pos.CENTER
		}
		
		CalendarTableView.cellLabelIcon {
			prefHeight = 14.px
			prefWidth = 14.px
		}
		
		CalendarTableView.cellAppointLabel {
			fontSize = 10.px
		}
		
		CalendarTableView.cellAppointTypeLabel {
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