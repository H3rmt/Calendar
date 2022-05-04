package frame.styles

import javafx.geometry.*
import javafx.scene.paint.*
import javafx.scene.text.*
import tornadofx.*

class OverviewStyles: Stylesheet() {
	companion object {
		val tableHeaderItem by cssclass()
		val selectedColumn by cssclass()
		val cell by cssclass()
		val disabledCell by cssclass()
		val hoveredTableCell by cssclass()
		val icon by cssclass()
		val cellLabel by cssclass()
		val cellAppointTypeLabel by cssclass()
		val cellAppointLabel by cssclass()
		val markedTableCell by cssclass()
	}
	
	init {
		tableHeaderItem {
			borderColor += box(Color.SILVER)
			borderRadius += box(10.px, 10.px, 0.px, 0.px)
			borderWidth += box(2.px)
		}
		selectedColumn {
			backgroundColor += c(0.92, 0.92, 0.90)
			backgroundRadius += box(6.px)
		}
		cell {
			backgroundColor += c(0.89, 0.89, 0.89)
			backgroundRadius += box(6.px)
			
			borderColor += box(c(0.75, 0.75, 0.75))
			borderRadius += box(6.px)
			borderWidth += box(2.px)
		}
		disabledCell {
			backgroundColor += c(0.98, 0.98, 0.98)
			backgroundRadius += box(6.px)
			borderWidth += box(2.px)
		}
		hoveredTableCell {
			borderColor += box(c(0, 151, 190), c(0, 136, 204), c(0, 151, 190), c(0, 136, 204))
			borderRadius += box(6.px)
			borderWidth += box(2.px)
		}
		icon {
			prefHeight = 14.px
			prefWidth = 14.px
		}
		cellLabel {
			fontSize = 14.px
			fontWeight = FontWeight.BOLD
			
			prefWidth = Int.MAX_VALUE.px
			alignment = Pos.CENTER
		}
		cellAppointTypeLabel {
			fontSize = 11.px
		}
		cellAppointLabel {
			fontSize = 10.px
		}
		markedTableCell {
			backgroundColor += Color.CORNFLOWERBLUE
			
			borderColor += box(Color.BLUE)
			borderRadius += box(6.px)
			borderWidth += box(2.px)
		}
	}
}
