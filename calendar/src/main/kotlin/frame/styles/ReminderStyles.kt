package frame.styles

import javafx.scene.layout.*
import javafx.scene.paint.*
import tornadofx.*

class ReminderStyles: Stylesheet() {
	companion object {
		val tableItemLeft by cssclass()
		val tableItemRight by cssclass()
		val tableItemMiddle by cssclass()
		val reminderRow by cssclass()
		val tableHeaderItem by cssclass()
	}
	
	init {
		tableItemLeft {
			borderColor += box(Color.SILVER)
			borderStyle += BorderStrokeStyle.DOTTED
			borderWidth += box(0.px, 2.px, 0.px, 0.px)
		}
		tableItemRight {
			borderColor += box(Color.SILVER)
			borderStyle += BorderStrokeStyle.DOTTED
			borderWidth += box(0.px, 0.px, 0.px, 2.px)
		}
		tableItemMiddle {
			borderColor += box(Color.SILVER)
			borderStyle += BorderStrokeStyle.DOTTED
			borderWidth += box(0.px, 2.px, 0.px, 2.px)
		}
		reminderRow {
			prefHeight = 40.px
			
			backgroundColor += c(0.98, 0.98, 0.98)
			backgroundRadius += box(6.px)
			borderWidth += box(2.px)
			
			borderColor += box(c(0.75, 0.75, 0.75))
			borderRadius += box(6.px)
			borderWidth += box(2.px)
		}
		tableHeaderItem {
			borderColor += box(Color.SILVER)
			borderRadius += box(10.px, 10.px, 0.px, 0.px)
			borderWidth += box(2.px)
		}
	}
}