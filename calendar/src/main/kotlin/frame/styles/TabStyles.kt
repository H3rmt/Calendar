package frame.styles

import javafx.scene.paint.*
import javafx.scene.text.*
import tornadofx.*

class TabStyles: Stylesheet() {
	companion object {
		val title by cssclass()
		val titleButton by cssclass()
		val tabContainer by cssclass()
		val tabContent by cssclass()
		val separator by cssclass()
		val topbar by cssclass()
		val shadowBorder by cssclass()
	}
	
	init {
		title {
			fontSize = 26.px
			fontWeight = FontWeight.BOLD
			textFill = Color.BLACK
		}
		
		titleButton {
			focusColor = Color.BLACK
			fontWeight = FontWeight.BOLD
		}
		
		tabContent {
//			borderColor += box(Color.BLACK)
//			borderWidth += box(3.px, 0.px, 0.px, 0.px)
		}
		
		tabContainer {
			padding = box(0.px)
		}
		
		separator {
			backgroundColor += Color.BLACK
			prefHeight = 1.px
			maxHeight = 1.px
			minHeight = 1.px
		}
		
		topbar {
			minHeight = 50.px
			maxHeight = 50.px
			backgroundColor += Color.WHITESMOKE
			
			borderColor += box(Color.BLACK)
			borderWidth += box(0.px, 0.px, 2.px, 0.px)
			
			padding = box(0.px, 15.px)
		}
		
		shadowBorder {
			borderColor += box(Color.BLACK)
			borderWidth += box(3.px)
			borderRadius += box(0.px)
		}
	}
}