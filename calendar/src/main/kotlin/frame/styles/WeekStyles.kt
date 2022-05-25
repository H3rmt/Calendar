package frame.styles

import frame.styles.GlobalStyles.Mixins.BGColor
import javafx.scene.paint.*
import tornadofx.*

class WeekStyles: Stylesheet() {
	
	companion object {
		val tableDay_ by cssclass()
		val TimeCell_ by cssclass()
		val TimeCellBorder_ by cssclass()
		val ActiveTimeCell_ by cssclass()
		
		val UnHoveredInnerTimeCell_ by cssclass()
		val HoveredInnerTimeCell_ by cssclass()
		
		val invisibleScrollbar_ by cssclass()
		
		val tableTimeHeader_ by cssclass()
	}
	
	init {
		tableDay_ {
			prefWidth = Int.MAX_VALUE.px
			
			backgroundColor += c(0.98, 0.98, 0.98)
			backgroundRadius += box(6.px)
			
			borderColor += box(c(0.75, 0.75, 0.75))
			borderRadius += box(2.px)
			borderWidth += box(2.px)
		}
		
		TimeCell_ {
			prefHeight = 40.px
			minHeight = prefHeight
			maxHeight = prefHeight
			
			+BGColor
		}
		
		TimeCellBorder_ {
			borderColor += box(c(0.75, 0.75, 0.75))
			borderWidth += box(0.px, 0.px, 2.px, 0.px)
		}
		
		ActiveTimeCell_ {
			backgroundColor += Color.DARKGRAY
		}
		
		UnHoveredInnerTimeCell_ {
			prefWidth = Int.MAX_VALUE.px
			prefHeight = 38.px    // - padding  - border bottom
			minHeight = prefHeight
			maxHeight = prefHeight
			
			padding = box(1.px)
			
			borderColor += box(Color.TRANSPARENT)
			borderWidth += box(2.px)
		}
		
		HoveredInnerTimeCell_ {
			borderColor += box(c(0, 151, 190), c(0, 136, 204), c(0, 151, 190), c(0, 136, 204))
		}
		
		tableTimeHeader_ {
			prefHeight = 28.px
			minHeight = prefHeight
			
			borderColor += box(c(0.75, 0.75, 0.75))
			borderRadius += box(4.px, 4.px, 0.px, 0.px)
			borderWidth += box(2.px)
		}
		
		invisibleScrollbar_ {
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
