package frame.styles

import javafx.geometry.Pos
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import tornadofx.*

class OverviewStyles: Stylesheet() {
	companion object {
		val headerItem_ by cssclass()
		val toggledRow_ by cssclass()

		val cell_ by cssclass()

		val disabledCell_ by cssclass()
		val hoveredCell_ by cssclass()
		val markedCell_ by cssclass()

		val cellIcon_ by cssclass()
		val cellLabel_ by cssclass()
		val cellAppointTypeLabel_ by cssclass()
		val cellAppointLabel_ by cssclass()
	}

	init {
		headerItem_ {
			borderRadius += box(10.px, 10.px, 0.px, 0.px)
			borderColor += box(Color.SILVER)
			borderWidth += box(2.px)

			minWidth = 85.px
		}
		toggledRow_ {
			backgroundRadius += box(6.px)
			backgroundColor += c(0.94, 0.94, 0.94)
			borderWidth += box(2.px)
		}

		cell_ {
			backgroundColor += c(0.89, 0.89, 0.89)
			backgroundRadius += box(6.px)

			borderColor += box(c(0.75, 0.75, 0.75))
			borderRadius += box(6.px)
			borderWidth += box(2.px)

			minWidth = 85.px
		}

		disabledCell_ {
			backgroundColor += c(0.98, 0.98, 0.98)
		}
		hoveredCell_ {
			borderColor += box(c(0, 151, 190), c(0, 136, 204), c(0, 151, 190), c(0, 136, 204))
		}
		markedCell_ {
			borderColor += box(Color.BLUE)
			backgroundColor += Color.CORNFLOWERBLUE
		}

		cellIcon_ {
			prefHeight = 14.px
			prefWidth = 14.px
		}
		cellLabel_ {
			fontSize = 14.px
			fontWeight = FontWeight.BOLD

			prefWidth = Int.MAX_VALUE.px
			alignment = Pos.CENTER
		}
		cellAppointTypeLabel_ {
			fontSize = 11.px
		}
		cellAppointLabel_ {
			fontSize = 10.px
		}
	}
}
