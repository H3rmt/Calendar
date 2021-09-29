package frame

import calendar.Day
import calendar.Week
import javafx.scene.control.*
import javafx.scene.text.*
import logic.LogType
import logic.log
import tornadofx.*

fun createWeekTab(pane: TabPane, week: Week, day: Day?): Tab {
	log("creating week tab", LogType.IMPORTANT)
	return pane.tab(week.toDate()) {
		isClosable = true
		
		stackpane {
			style(append = true) {
				padding = box(6.px)
			}
			
			vbox {
				addClass(Styles.Tabs.mainTab)
				label("$week") {
					style {
						fontSize = 18.px
						fontWeight = FontWeight.BOLD
					}
				}
				for((key, value) in week.getallAppointmentsSorted()) {
					label(key.name) {
						style {
							fontSize = 14.px
							fontWeight = FontWeight.BOLD
						}
					}
					for(ap in value) {
						label {
							text = "$ap"
						}
					}
				}
			}
			
			// used to shadow the overflow from tab
			pane {
				isMouseTransparent = true
				addClass(Styles.Tabs.shadowBorder)
			}
		}
	}
}