package frame.styles

import frame.styles.GlobalStyles.Mixins.BGColor
import frame.styles.GlobalStyles.Mixins.SecColor
import javafx.geometry.*
import javafx.scene.paint.*
import javafx.scene.text.*
import tornadofx.*

class GlobalStyles: Stylesheet() {
	companion object {
		val background_ by cssclass()
		val secBackground_ by cssclass()
		
		val disableFocusDraw_ by cssclass()
		val maxHeight_ by cssclass()
		
		val table_ by cssclass()
		val tableHeader_ by cssclass()
		val tableItem_ by cssclass()
		val tableHeaderItem_ by cssclass()
	}
	
	object Mixins {
		val BGColor = mixin {
			backgroundColor += Color.LIGHTGREY
		}
		val SecColor = mixin {
			backgroundColor += Color.DARKGRAY
		}
	}
	
	init {
		background_ {
			+BGColor
		}
		secBackground_ {
			+SecColor
		}
		disableFocusDraw_ {
			focusColor = Color.TRANSPARENT
		}
		maxHeight_ {
			prefHeight = Int.MAX_VALUE.px
		}
		
		table_ {
			+BGColor
			prefHeight = Int.MAX_VALUE.px
			padding = box(2.px)
		}
		tableHeader_ {
			+BGColor
			padding = box(8.px, 3.px, 3.px, 3.px) // 15.3 because of scrollbar width (fixed initial scrollbar)
		}
		tableItem_ {
			alignment = Pos.CENTER
			prefWidth = Int.MAX_VALUE.px
			
			padding = box(2.px)
		}
		tableHeaderItem_ {
			prefHeight = 30.px
			minHeight = prefHeight
			
			fontSize = 13.px
			fontWeight = FontWeight.BOLD
		}
	}
}
