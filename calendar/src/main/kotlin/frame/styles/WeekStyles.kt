package frame.styles

import javafx.scene.paint.*
import tornadofx.*

class WeekStyles: Stylesheet() {
	
	companion object {
		val tableDay by cssclass()
		val TimeCell by cssclass()
		val TimeCellBorder by cssclass()
		val ActiveTimeCell by cssclass()
		
		val UnHoveredInnerTimeCell by cssclass()
		val HoveredInnerTimeCell by cssclass()
		
		val invisibleScrollbar by cssclass()
		
		val tableTimeHeader by cssclass()
	}
	
	init {
		tableDay {
			prefWidth = Int.MAX_VALUE.px
			
			backgroundColor += c(0.98, 0.98, 0.98)
			backgroundRadius += box(6.px)
			
			borderColor += box(c(0.75, 0.75, 0.75))
			borderRadius += box(2.px)
			borderWidth += box(2.px)
		}
		
		TimeCell {
			prefHeight = 40.px
			minHeight = prefHeight
			maxHeight = prefHeight
		}
		
		TimeCellBorder {
			borderColor += box(c(0.75, 0.75, 0.75))
			borderWidth += box(0.px, 0.px, 2.px, 0.px)
		}
		
		ActiveTimeCell {
			backgroundColor += Color.DARKGRAY
		}
		
		UnHoveredInnerTimeCell {
			prefWidth = Int.MAX_VALUE.px
			prefHeight = 38.px    // - padding  - border bottom
			minHeight = prefHeight
			maxHeight = prefHeight
			
			padding = box(1.px)
			
			borderColor += box(Color.TRANSPARENT)
			borderWidth += box(2.px)
		}
		
		HoveredInnerTimeCell {
			borderColor += box(c(0, 151, 190), c(0, 136, 204), c(0, 151, 190), c(0, 136, 204))
		}
		
		tableTimeHeader {
			prefHeight = 28.px
			minHeight = prefHeight
			
			borderColor += box(c(0.75, 0.75, 0.75))
			borderRadius += box(4.px, 4.px, 0.px, 0.px)
			borderWidth += box(2.px)
		}
		
		invisibleScrollbar {
			scrollBar {
				backgroundColor += Color.WHITE
				
				decrementArrow {
					prefHeight = 0.px
				}
				incrementArrow {
					prefHeight = 0.px
				}
			}
		}
	}
}