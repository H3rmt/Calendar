package frame.styles

import javafx.scene.paint.*
import javafx.scene.text.*
import tornadofx.*

class MenubarStyles: Stylesheet() {
	companion object {
		val shortcut_ by cssclass()
		val itemName_ by cssclass()
		val gridPane_ by cssclass()
		val spacing_ by cssclass()
	}
	
	init {
		shortcut_ {
			fontSize = 9.px
			fontWeight = FontWeight.THIN
			textFill = Color.DARKGRAY
		}
		
		itemName_ {
			fontSize = 12.px
			fontWeight = FontWeight.NORMAL
			textFill = Color.BLACK
		}
		
		gridPane_ {
			maxWidth = 300.px
		}
		
		spacing_ {
			minWidth = 15.px
		}
	}
}
