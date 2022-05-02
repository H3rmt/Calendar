package frame.styles

import javafx.scene.paint.*
import tornadofx.*

class OverviewStyles: Stylesheet() {
	companion object {
		val tableHeaderItem by cssclass()
		val selectedColumn by cssclass()
		val cell by cssclass()
		val disabledCell by cssclass()
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
	}
}
