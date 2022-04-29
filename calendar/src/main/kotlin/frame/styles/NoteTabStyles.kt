package frame.styles

import javafx.scene.paint.*
import tornadofx.*

class NoteTabStyles: Stylesheet() {
	companion object {
		val notesPane by cssclass()
		val paneToolbar by cssclass()
		val editor by cssclass()
	}
	
	init {
		notesPane {
			padding = box(2.px)
		}
		
		paneToolbar {
			backgroundColor += Color.TRANSPARENT
			padding = box(0.px, 0.px, 0.px, 20.px)
		}
		
		editor {
			maxHeight = 300.px
		}
	}
}