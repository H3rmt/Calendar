package frame.styles

import frame.styles.GlobalStyles.Mixins.BGColor
import javafx.scene.layout.BorderStrokeStyle
import javafx.scene.paint.Color
import tornadofx.*

class ReminderStyles: Stylesheet() {
	companion object {
		val tableItemLeft_ by cssclass()
		val tableItemRight_ by cssclass()
		val tableItemMiddle_ by cssclass()
		val reminderRow_ by cssclass()
		val tableHeaderItem_ by cssclass()
	}

	init {
		tableItemLeft_ {
			borderColor += box(Color.SILVER)
			borderStyle += BorderStrokeStyle.DOTTED
			borderWidth += box(0.px, 2.px, 0.px, 0.px)
		}
		tableItemRight_ {
			borderColor += box(Color.SILVER)
			borderStyle += BorderStrokeStyle.DOTTED
			borderWidth += box(0.px, 0.px, 0.px, 2.px)
		}
		tableItemMiddle_ {
			borderColor += box(Color.SILVER)
			borderStyle += BorderStrokeStyle.DOTTED
			borderWidth += box(0.px, 2.px, 0.px, 2.px)
		}
		reminderRow_ {
			prefHeight = 40.px

			+BGColor
			backgroundRadius += box(6.px)
			borderWidth += box(2.px)

			borderColor += box(c(0.75, 0.75, 0.75))
			borderRadius += box(6.px)
			borderWidth += box(2.px)
		}
		tableHeaderItem_ {
			borderColor += box(Color.SILVER)
			borderRadius += box(10.px, 10.px, 0.px, 0.px)
			borderWidth += box(2.px)
		}
	}
}
