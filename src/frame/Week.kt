package frame

import calendar.Day
import calendar.Week
import javafx.geometry.*
import javafx.scene.control.*
import javafx.scene.paint.*
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
				style {
					padding = box(0.px, 0.px, 2.px, 0.px)
				}
				
				hbox(spacing = 20.0, alignment = Pos.CENTER_LEFT) {
					addClass(Styles.Tabs.topbar)
					style {
						padding = box(0.px, 15.px, 0.px, 15.px)
					}
					button {
						text = "Test"
						
						addClass(Styles.Tabs.titleButtons)
					}
				}
				
				separate()
				
				log("creating table view", LogType.LOW)
				// Table view
				vbox(spacing = 1.0, alignment = Pos.TOP_CENTER) {
					addClass(Styles.CalendarTableView.table)
					style(append = true) {
						backgroundColor += Color.GREEN
					}
					// Top bar
					hbox(spacing = 5.0, alignment = Pos.CENTER) {
						padding = Insets(3.0, 18.0, 3.0, 3.0)
						
						label("") {
							addClass(Styles.CalendarTableView.tableItem)
						}
						label("Monday") {
							addClass(Styles.CalendarTableView.tableItem)
							addClass(Styles.CalendarTableView.tableHeader)
							addClass(Styles.CalendarTableView.cellHeaderLabel)
						}
						label("Tuesday") {
							addClass(Styles.CalendarTableView.tableItem)
							addClass(Styles.CalendarTableView.tableHeader)
							addClass(Styles.CalendarTableView.cellHeaderLabel)
						}
						label("Wednesday") {
							addClass(Styles.CalendarTableView.tableItem)
							addClass(Styles.CalendarTableView.tableHeader)
							addClass(Styles.CalendarTableView.cellHeaderLabel)
						}
						label("Thursday") {
							addClass(Styles.CalendarTableView.tableItem)
							addClass(Styles.CalendarTableView.tableHeader)
							addClass(Styles.CalendarTableView.cellHeaderLabel)
						}
						label("Friday") {
							addClass(Styles.CalendarTableView.tableItem)
							addClass(Styles.CalendarTableView.tableHeader)
							addClass(Styles.CalendarTableView.cellHeaderLabel)
						}
						label("Saturday") {
							addClass(Styles.CalendarTableView.tableItem)
							addClass(Styles.CalendarTableView.tableHeader)
							addClass(Styles.CalendarTableView.cellHeaderLabel)
						}
						label("Sunday") {
							addClass(Styles.CalendarTableView.tableItem)
							addClass(Styles.CalendarTableView.tableHeader)
							addClass(Styles.CalendarTableView.cellHeaderLabel)
						}
					}
					
					fun updateTable() {
						log("updated table view", LogType.LOW)
						scrollpane(fitToWidth = true) {
							style {
								backgroundColor += Color.RED
								prefHeight = Int.MAX_VALUE.px
							}
							for(hour in 0..10) {
								hbox(spacing = 5.0, alignment = Pos.CENTER) {
									padding = Insets(3.0)
									
									vbox() {
										style(append = true) {
											backgroundColor += Color.LIGHTSEAGREEN
										}
										addClass(Styles.CalendarTableView.tableItem)
										addClass(Styles.WeekTab.tablecell)
									}
									for((dayOfWeek, _day) in week.allDays) {
										vbox() {
											style(append = true) {
												backgroundColor += Color.GOLD
											}
											addClass(Styles.CalendarTableView.tableItem)
											addClass(Styles.WeekTab.tablecell)
										}
										
									}
								}
							}
						}
					}

//					currentMonth.addListener(ListChangeListener {
//						updateTable()
//					})
					
					updateTable()
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