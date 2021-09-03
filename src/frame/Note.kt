package frame

import calendar.Celldisplay
import javafx.scene.control.*
import javafx.scene.paint.*
import logic.LogType
import logic.log
import tornadofx.*


fun createnotetab(pane: TabPane, cell: Celldisplay): Tab {
	log("creating week tab", LogType.IMPORTANT)
	return pane.tab("note ") { // "Notes for ${data.time.dayOfMonth}/${data.time.month}"
		isClosable = true
		
		stackpane {
			style(append = true) {
				//maxHeight = 500.px
				padding = box(6.px)
			}
			
			vbox {
				style {
					borderColor += box(Color.TRANSPARENT)
					borderWidth += box(5.px)
					borderRadius += box(10.px)
				}
				label { cell.notes }
			}
			
			// used to shadow the overflow from tab
			pane {
				isMouseTransparent = true
				style {
					borderColor += box(Color.BLACK)
					borderWidth += box(5.px)
					borderRadius += box(10.px)
				}
			}
		}
	}
}