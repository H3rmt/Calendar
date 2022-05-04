package frame.styles

import javafx.geometry.*
import javafx.scene.paint.*
import javafx.scene.text.*
import tornadofx.*

class OverviewStyles: Stylesheet() {
	companion object {
		val headerItem by cssclass()
		val toggledRow by cssclass()
		
		val cell by cssclass()
		
		val disabledCell by cssclass()
		val hoveredCell by cssclass()
		val markedCell by cssclass()
		
		val cellIcon by cssclass()
		val cellLabel by cssclass()
		val cellAppointTypeLabel by cssclass()
		val cellAppointLabel by cssclass()
	}
	
	init {
		headerItem {
			borderRadius += box(10.px, 10.px, 0.px, 0.px)
			borderColor += box(Color.SILVER)
			borderWidth += box(2.px)
			
			minWidth = 85.px
		}
		toggledRow {
			backgroundRadius += box(6.px)
			backgroundColor += c(0.94, 0.94, 0.94)
			borderWidth += box(2.px)
		}
		
		cell {
			backgroundColor += c(0.89, 0.89, 0.89)
			backgroundRadius += box(6.px)
			
			borderColor += box(c(0.75, 0.75, 0.75))
			borderRadius += box(6.px)
			borderWidth += box(2.px)
			
			minWidth = 85.px
		}
		
		disabledCell {
			backgroundColor += c(0.98, 0.98, 0.98)
		}
		hoveredCell {
			borderColor += box(c(0, 151, 190), c(0, 136, 204), c(0, 151, 190), c(0, 136, 204))
		}
		markedCell {
			borderColor += box(Color.BLUE)
			backgroundColor += Color.CORNFLOWERBLUE
		}
		
		cellIcon {
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
	}
}
