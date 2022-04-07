package frame

import calendar.Reminder
import calendar.Timing
import javafx.geometry.*
import javafx.scene.control.*
import logic.LogType
import logic.getLangString
import logic.log
import popup.NewReminderPopup
import tornadofx.*


fun createReminderTab(pane: TabPane, updateCallback: () -> Unit): Tab {
	log("creating week tab", LogType.IMPORTANT)
	return pane.tab(getLangString("reminder")) {
		isClosable = true
		
		stackpane {
			style(append = true) {
				padding = box(6.px)
			}
			
			// main tab
			vbox {
				addClass(Styles.Tabs.mainTab)
				style {
					padding = box(0.px, 0.px, 2.px, 0.px)
				}
				
				hbox(spacing = 40.0, alignment = Pos.CENTER_LEFT) {
					addClass(Styles.Tabs.topbar)
					style {
						padding = box(0.px, 15.px, 0.px, 15.px)
					}
					button {
						text = getLangString("New Reminder")
						addClass(Styles.Tabs.titleButtons)
						
						action {
							NewReminderPopup.open(getLangString("new reminder"), getLangString("Create"),
								false,
								null,
								Timing.getNowLocal(),
								save = { rem: Reminder ->
									log("Created:$rem")
								}
							)
						}
					}
				}
				
				separate()
			}
			
			// used to shadow the overflow from tab
			pane {
				isMouseTransparent = true
				addClass(Styles.Tabs.shadowBorder)
			}
		}
		
	}
}
