package frame.styles

import frame.styles.GlobalStyles.Mixins.BGColor
import frame.styles.GlobalStyles.Mixins.SecColor
import javafx.geometry.*
import javafx.scene.paint.*
import javafx.scene.text.*
import tornadofx.*

class GlobalStyles: Stylesheet() {
	companion object {
		val background by cssclass()
		val secBackground by cssclass()
		
		val disableFocusDraw by cssclass()
		val maxHeight by cssclass()
		
		val table by cssclass()
		val tableHeader by cssclass()
		val tableItem by cssclass()
		val tableHeaderItem by cssclass()
	}
	
	object Mixins {
		val BGColor = mixin {
			backgroundColor += Color.WHITE
		}
		val SecColor = mixin {
			backgroundColor += Color.LIGHTGREY
		}
	}
	
	init {
		background {
			+BGColor
		}
		secBackground {
			+SecColor
		}
		disableFocusDraw {
			focusColor = Color.TRANSPARENT
		}
		maxHeight {
			prefHeight = Int.MAX_VALUE.px
		}
		
		table {
			+BGColor
			prefHeight = Int.MAX_VALUE.px
			padding = box(2.px)
		}
		tableHeader {
			+BGColor
			padding = box(8.px, 3.px, 3.px, 3.px) // 15.3 because of scrollbar width (fixed initial scrollbar)
		}
		tableItem {
			alignment = Pos.CENTER
			prefWidth = Int.MAX_VALUE.px
			
			padding = box(2.px)
		}
		tableHeaderItem {
			prefHeight = 30.px
			minHeight = prefHeight
			
			fontSize = 13.px
			fontWeight = FontWeight.BOLD
		}
	}
}
