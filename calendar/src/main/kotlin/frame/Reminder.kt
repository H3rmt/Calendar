package frame

import calendar.Reminder
import calendar.reminders
import frame.styles.GlobalStyles
import frame.styles.ReminderStyles
import frame.styles.TabStyles
import javafx.beans.property.*
import javafx.collections.*
import javafx.geometry.*
import javafx.scene.control.*
import listen
import logic.LogType
import logic.getLangString
import logic.log
import tornadofx.*


fun createReminderTab(pane: TabPane): Tab {
	log("creating week tab", LogType.IMPORTANT)
	return pane.tab(getLangString("reminders")) {
		isClosable = false
		addClass(TabStyles.tab)
		
		vbox {
			hbox(spacing = 20.0, alignment = Pos.CENTER) {
				addClass(TabStyles.topbar)
				label(getLangString("reminder")) {
					addClass(TabStyles.title)
					minWidth = 200.0
					alignment = Pos.CENTER
				}
			}
			
			vbox(spacing = 1.0, alignment = Pos.TOP_CENTER) {
				addClass(GlobalStyles.disableFocusDraw)
				addClass(GlobalStyles.table)
				
				lateinit var scrollbarWidth: DoubleProperty
				
				// Top bar
				hbox(spacing = 5.0, alignment = Pos.CENTER) {
					addClass(GlobalStyles.tableHeader)
					
					scrollbarWidth = paddingRightProperty
					for(header in arrayListOf("Title", "Deadline", "Description")) {
						label(getLangString(header)) {
							addClass(GlobalStyles.tableItem)
							addClass(GlobalStyles.tableHeaderItem)
							addClass(ReminderStyles.tableHeaderItem)
						}
					}
				}
				
				var table: ScrollPane? = null
				
				fun updateTable(list: ObservableList<out Reminder>) {
					children.remove(table)
					log("updated table view", LogType.LOW)
					
					table = scrollpane(fitToWidth = true) {
						addClass(GlobalStyles.disableFocusDraw)
						addClass(GlobalStyles.maxHeight)
						addClass(GlobalStyles.background)
						
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
						
						vbox(spacing = 5.0, alignment = Pos.CENTER) {
							addClass(GlobalStyles.background)
							for(reminder in list) {
								hbox(spacing = 5.0, alignment = Pos.CENTER) {
									addClass(ReminderStyles.reminderRow)
									label(reminder.title) {
										addClass(GlobalStyles.tableItem)
										addClass(ReminderStyles.tableItemLeft)
									}
									label(reminder.time.toString()) {
										addClass(GlobalStyles.tableItem)
										addClass(ReminderStyles.tableItemMiddle)
									}
									label(reminder.description) {
										addClass(GlobalStyles.tableItem)
										addClass(ReminderStyles.tableItemRight)
									}
								}
							}
						}
					}
				}
				
				reminders.addListener(ListChangeListener {
					updateTable(it.list)
				})
				
				updateTable(reminders)
			}
		}
	}
}
