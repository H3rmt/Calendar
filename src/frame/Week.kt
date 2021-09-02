package frame

import calendar.Day
import calendar.Week
import javafx.scene.control.*
import javafx.scene.paint.*
import javafx.scene.text.*
import logic.LogType
import logic.log
import tornadofx.*

fun createweektab(pane: TabPane, week: Week, day: Day?): Tab {
	log("creating week tab", LogType.IMPORTANT)
	return pane.tab(week.toDate()) {
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
				label(week.toString()) {
					style {
						fontSize = 18.px
						fontWeight = FontWeight.BOLD
					}
				}
				for(app in week.getallappointmentssort()) {
					label(app.key.name) {
						style {
							fontSize = 14.px
							fontWeight = FontWeight.BOLD
						}
					}
					for(ap in app.value) {
						label {
							text = ap.toString()
						}
					}
				}
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