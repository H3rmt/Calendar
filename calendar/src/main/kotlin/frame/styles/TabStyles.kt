package frame.styles

import frame.styles.GlobalStyles.Mixins.SecColor
import javafx.scene.paint.*
import javafx.scene.text.*
import tornadofx.*

class TabStyles: Stylesheet() {
	companion object {
		val tab_ by cssclass()
		val title_ by cssclass()
		val titleButton_ by cssclass()
		val tabContainer_ by cssclass()
		val topbar_ by cssclass()
		val content_ by cssclass()
	}
	
	init {
		tab_ {
			+SecColor
//			borderColor += box(backgroundColor.elements[0]) // equal to background_
//			borderWidth += box(0.px, 0.px, 1.px, 0.px)  // TODO do this if tab open
		}
		
		title_ {
			fontSize = 26.px
			fontWeight = FontWeight.BOLD
			textFill = Color.BLACK
		}
		
		titleButton_ {
			focusColor = Color.BLACK
			fontWeight = FontWeight.BOLD
		}
		
		tabContainer_ {
			padding = box(0.px)
		}
		
		topbar_ {
			minHeight = 50.px
			maxHeight = 50.px
			+SecColor
			
			borderColor += box(Color.BLACK)
			borderWidth += box(0.px, 0.px, 2.px, 0.px)
			
			padding = box(0.px, 15.px)
		}
		
		content_ {
//			backgroundColor += Color.BLUE
		}
	}
}