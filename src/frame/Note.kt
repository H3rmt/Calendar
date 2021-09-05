package frame

import calendar.Celldisplay
import calendar.Day
import calendar.Week
import javafx.scene.control.*
import javafx.scene.paint.*
import logic.LogType
import logic.getLangString
import logic.log
import tornadofx.*
import java.time.temporal.ChronoField


fun createnotetab(pane: TabPane, cell: Celldisplay): Tab {
	log("creating week tab", LogType.IMPORTANT)
	return pane.tab("") {
		if(cell is Day)
			text = "Notes for ${cell.time.dayOfMonth}. ${getLangString(cell.time.month.name)}"
		else if(cell is Week)
			text = "Notes for ${cell.time.get(ChronoField.ALIGNED_WEEK_OF_MONTH)}. Week in ${getLangString(cell.time.month.name)}"
		
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