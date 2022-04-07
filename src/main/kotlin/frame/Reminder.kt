package frame

import calendar.Reminder
import calendar.Timing
import calendar.reminders
import javafx.beans.property.*
import javafx.collections.*
import javafx.geometry.*
import javafx.scene.control.*
import javafx.scene.paint.*
import listen
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
				
				log("creating top bar", LogType.LOW)
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
					label(getLangString("reminder")) {
						addClass(Styles.Tabs.title)
						minWidth = 200.0
						alignment = Pos.CENTER
					}
				}
				
				separate()
				log("creating table view", LogType.LOW)
				// Table view
				vbox(spacing = 1.0, alignment = Pos.TOP_CENTER) {
					addClass(Styles.CalendarTableView.table)
					addClass(Styles.disableFocusDraw)
					
					lateinit var scrollbarWidth: DoubleProperty
					
					// Top bar
					hbox(spacing = 5.0, alignment = Pos.CENTER) {
						padding = Insets(3.0)
						scrollbarWidth = paddingRightProperty
						for(day in arrayListOf("Deadline", "Title", "Description")) {
							label(getLangString(day)) {
								addClass(Styles.CalendarTableView.tableItem)
								addClass(Styles.CalendarTableView.tableHeader)
								addClass(Styles.CalendarTableView.cellHeaderLabel)
							}
						}
					}
					
					var table: ScrollPane? = null
					
					fun updateTable(list: ObservableList<out Reminder>) {
						children.remove(table)
						log("updated table view", LogType.LOW)
						
						table = scrollpane(fitToWidth = true) {
							
							// update top bar fake scrollbar padding  (wait for width update,so that scrollbars were created already; and then update if scrollbar width changes[appears/disappears])
							widthProperty().listen(once = true) {
								lookupAll(".scroll-bar").filterIsInstance<ScrollBar>().filter { it.orientation == Orientation.VERTICAL }[0].let { bar ->
									bar.visibleProperty().listen { visible ->
										if(visible) {
											scrollbarWidth.value = bar.width + 2 // 2 padding right of inner vbox
										} else {
											scrollbarWidth.value = 2.0 // 2 padding right of inner vbox
										}
									}
								}
							}
							
							style {
								borderWidth += box(1.px)
								borderColor += box(Color.WHITE)
								prefHeight = Int.MAX_VALUE.px
							}
							
							vbox(spacing = 5.0, alignment = Pos.CENTER) {
								style(append = true) {
									backgroundColor += Color.WHITE
								}
								for((index, reminder) in list.withIndex()) {
									hbox(spacing = 5.0, alignment = Pos.CENTER) {
										style(append = true) {
											backgroundColor += Color.ORANGE
										}
										
										val data = mutableListOf<Label>()
										
										data.add(label(reminder.time.toString()))
										data.add(label(reminder.title))
										data.add(label(reminder.description))
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
			
			// used to shadow the overflow from tab
			pane {
				isMouseTransparent = true
				addClass(Styles.Tabs.shadowBorder)
			}
		}
		
	}
}
