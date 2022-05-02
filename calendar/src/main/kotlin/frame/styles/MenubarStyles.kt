package frame.styles

import javafx.scene.paint.*
import javafx.scene.text.*
import tornadofx.*

class MenubarStyles: Stylesheet() {
	companion object {
		val shortcut by cssclass()
		val itemName by cssclass()
		val gridPane by cssclass()
		val spacing by cssclass()
	}
	
	init {
		shortcut {
			fontSize = 9.px
			fontWeight = FontWeight.THIN
			textFill = Color.DARKGRAY
		}
		
		itemName {
			fontSize = 12.px
			fontWeight = FontWeight.NORMAL
			textFill = Color.BLACK
		}
		
		gridPane {
			maxWidth = 300.px
		}
		
		spacing {
			minWidth = 15.px
		}
	}
}
