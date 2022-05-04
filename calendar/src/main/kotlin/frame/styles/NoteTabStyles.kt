package frame.styles

import javafx.scene.paint.*
import tornadofx.*

class NoteTabStyles: Stylesheet() {
	companion object {
		val notesPane_ by cssclass()
		val paneToolbar_ by cssclass()
		val editor_ by cssclass()
	}
	
	init {
		notesPane_ {
			padding = box(2.px)
		}
		
		paneToolbar_ {
			backgroundColor += Color.TRANSPARENT
			padding = box(0.px, 0.px, 0.px, 20.px)
		}
		
		editor_ {
			maxHeight = 300.px
		}
	}
}
