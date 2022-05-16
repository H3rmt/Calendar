package frame.tabs

import calendar.Reminder
import calendar.Reminders
import frame.styles.GlobalStyles
import frame.styles.ReminderStyles
import frame.styles.TabStyles
import javafx.beans.property.*
import javafx.geometry.*
import javafx.scene.control.*
import listen
import logic.Language
import logic.LogType
import logic.log
import logic.translate
import tornadofx.*


fun createReminderTab(pane: TabPane): Tab {
	log("creating week tab", LogType.IMPORTANT)
	return pane.tab("reminders".translate(Language.TranslationTypes.Reminder)) {
		isClosable = false
		addClass(TabStyles.tab_)
		
		vbox {
			hbox(spacing = 20.0, alignment = Pos.CENTER) {
				addClass(TabStyles.topbar_)
				label("reminder".translate(Language.TranslationTypes.Reminder)) {
					addClass(TabStyles.title_)
					minWidth = 200.0
					alignment = Pos.CENTER
				}
			}
			
			vbox(spacing = 1.0, alignment = Pos.TOP_CENTER) {
				addClass(GlobalStyles.disableFocusDraw_)
				addClass(GlobalStyles.table_)
				
				lateinit var scrollbarWidth: DoubleProperty
				
				// Top bar
				hbox(spacing = 5.0, alignment = Pos.CENTER) {
					addClass(GlobalStyles.tableHeader_)
					
					scrollbarWidth = paddingRightProperty
					for(header in arrayListOf("Title", "Deadline", "Description")) {
						label(header.translate(Language.TranslationTypes.Reminder)) {
							addClass(GlobalStyles.tableItem_)
							addClass(GlobalStyles.tableHeaderItem_)
							addClass(ReminderStyles.tableHeaderItem_)
						}
					}
				}
				
				var table: ScrollPane? = null
				
				fun updateTable(list: List<Reminder>) {
					children.remove(table)
					log("updated table_ view", LogType.LOW)
					
					table = scrollpane(fitToWidth = true, fitToHeight = true) {
						addClass(GlobalStyles.disableFocusDraw_)
						addClass(GlobalStyles.maxHeight_)
						addClass(GlobalStyles.background_)
						isPannable = true
						
						// update top bar fake scrollbar padding  (wait for width update,so that scrollbars were created already; and then update if scrollbar width changes[appears/disappears])
						widthProperty().listen(once = true) {
							lookupAll(".scroll-bar").filterIsInstance<ScrollBar>().filter { it.orientation == Orientation.VERTICAL }[0].let { bar ->
								bar.visibleProperty().listen { visible ->
									if(visible) { // 20 on first visible;  13.33  on second visible  => hardcoded 13.3 width TODO add to calender
										scrollbarWidth.value = 13.3 + 2.0 // bar.width + 2.0 // 2 padding right of inner vbox
									} else {
										scrollbarWidth.value = 2.0 // 2 padding right of inner vbox
									}
								}
							}
						}
						
						// gets stretched across whole scrollpane
						vbox(spacing = 2.0, alignment = Pos.TOP_CENTER) {
							addClass(GlobalStyles.background_)
							for(reminder in list) {
								hbox(spacing = 5.0, alignment = Pos.CENTER) {
									addClass(ReminderStyles.reminderRow_)
									label(reminder.title) {
										addClass(GlobalStyles.tableItem_)
										addClass(ReminderStyles.tableItemLeft_)
									}
									label(reminder.time) {
										addClass(GlobalStyles.tableItem_)
										addClass(ReminderStyles.tableItemMiddle_)
									}
									label(reminder.description) {
										addClass(GlobalStyles.tableItem_)
										addClass(ReminderStyles.tableItemRight_)
									}
								}
							}
						}
					}
				}
				
				Reminders.listen {
					updateTable(it.list)
				}
				
				updateTable(Reminders)
			}
		}
	}
}
