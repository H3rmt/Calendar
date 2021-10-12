package frame

import calendar.Day
import calendar.Week
import calendar.now
import javafx.event.*
import javafx.geometry.*
import javafx.scene.control.*
import javafx.scene.paint.*
import logic.LogType
import logic.log
import tornadofx.*
import java.time.temporal.IsoFields

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
					// Top bar
					scrollpane(fitToWidth = true) {
						vbarPolicy = ScrollPane.ScrollBarPolicy.ALWAYS
						
						addClass(Styles.WeekTab.invisibleScrollbar)
						style {
							borderWidth += box(1.px)
							borderColor += box(Color.WHITE)
							backgroundColor += Color.WHITE
							
							prefHeight = 38.px
							minHeight = prefHeight
							
							padding = box(3.px, 4.px, 3.px, 6.px)
						}
						
						hbox(spacing = 2.0, alignment = Pos.CENTER) {
							style {
								backgroundColor += Color.WHITE
							}
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
					}
					
					fun updateTable() {
						log("updated table view", LogType.LOW)
						scrollpane(fitToWidth = true) {
							style {
								borderWidth += box(1.px)
								borderColor += box(Color.WHITE)
							}
							
							hbox(spacing = 2.0, alignment = Pos.CENTER) {
								style(append = true) {
									padding = box(4.px)
									backgroundColor += Color.WHITE
								}
								vbox(alignment = Pos.TOP_CENTER) {
									addClass(Styles.WeekTab.tableDay)
									for(hour in 0..23) {
										vbox(alignment = Pos.CENTER) {
											addClass(Styles.WeekTab.TimeCell)
											
											if(now.hour == hour && week.time.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR) == now.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR) && week.time.year == now.year)
												addClass(Styles.WeekTab.ActiveTimeCell)
											
											label {
												style {
													fontSize = 20.px
												}
												text = String.format("%02d:00", hour)
											}
										}
									}
								}
								for((_, _) in week.allDays) {
									vbox(alignment = Pos.TOP_CENTER) {
										addClass(Styles.WeekTab.tableDay)
										for(hour in 0..23) {
											hbox {
												addClass(Styles.WeekTab.TimeCell)
												
												if(now.hour == hour && week.time.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR) == now.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR) && week.time.year == now.year)
													addClass(Styles.WeekTab.ActiveTimeCell)
												
												vbox {
													label {
														text = "hi das text"
													}
													label {
														text = "das text"
													}
													
													addClass(Styles.WeekTab.UnHoveredInnerTimeCell)
													onMouseEntered = EventHandler {
														addClass(Styles.WeekTab.HoveredInnerTimeCell)
													}
													onMouseExited = EventHandler {
														removeClass(Styles.WeekTab.HoveredInnerTimeCell)
													}
												}
											}
										}
									}
								}
							}
						}
					}
					
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