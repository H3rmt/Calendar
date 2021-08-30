package frame

import calendar.Day
import calendar.Week
import javafx.scene.control.*
import javafx.scene.paint.*
import javafx.scene.text.*
import tornadofx.*

fun createweektab(pane: TabPane, week: Week, day: Day?): Tab {
	return pane.tab(week.toString()) {
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
				label(day.toString()) {
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
							text = ap.description
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