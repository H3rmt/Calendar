package frame.styles

import frame.styles.GlobalStyles.Mixins.SecColor
import javafx.scene.paint.*
import javafx.scene.text.*
import tornadofx.*

class TabStyles: Stylesheet() {
	companion object {
		val tab by cssclass()
		val title by cssclass()
		val titleButton by cssclass()
		val tabContainer by cssclass()
		val topbar by cssclass()
		val content by cssclass()
	}
	
	init {
		tab {
			+SecColor
		}
		
		title {
			fontSize = 26.px
			fontWeight = FontWeight.BOLD
			textFill = Color.BLACK
		}
		
		titleButton {
			focusColor = Color.BLACK
			fontWeight = FontWeight.BOLD
		}
		
		tabContainer {
			padding = box(0.px)
		}
		
		topbar {
			minHeight = 50.px
			maxHeight = 50.px
			+SecColor
			
			borderColor += box(Color.BLACK)
			borderWidth += box(0.px, 0.px, 2.px, 0.px)
			
			padding = box(0.px, 15.px)
		}
		
		content {
//			backgroundColor += Color.BLUE
		}
	}
}
