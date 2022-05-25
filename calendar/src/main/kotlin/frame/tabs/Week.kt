package frame.tabs

import calendar.Appointment
import calendar.Day
import calendar.Timing
import calendar.Week
import calendar.now
import frame.popup.AppointmentPopup
import frame.styles.GlobalStyles
import frame.styles.TabStyles
import frame.styles.WeekStyles
import javafx.beans.property.*
import javafx.event.*
import javafx.geometry.*
import javafx.scene.control.*
import javafx.scene.layout.*
import listen
import logic.Language
import logic.LogType
import logic.log
import logic.translate
import tornadofx.*
import java.time.LocalDateTime
import java.time.temporal.IsoFields



fun createWeekTab(pane: TabPane, week: Week, _day: Day?, updateCallback: () -> Unit): Tab {
	log("creating week tab", LogType.IMPORTANT)
	return pane.tab("") {
		text = "${week.time.dayOfMonth} - ${week.time.plusDays(6).dayOfMonth} / ${week.time.month.name.translate(Language.TranslationTypes.Global)}"
		isClosable = true
		addClass(TabStyles.tab_)
		
		// mainTab
		vbox {
			hbox(spacing = 40.0, alignment = Pos.CENTER) {
				addClass(TabStyles.topbar_)
				label("Week".translate(Language.TranslationTypes.Week)) {
					addClass(TabStyles.title_)
					alignment = Pos.CENTER
				}
			}
			
			log("creating table_ view", LogType.LOW)
			// Table view
			vbox(spacing = 1.0, alignment = Pos.TOP_CENTER) {
				addClass(GlobalStyles.disableFocusDraw_)
				addClass(GlobalStyles.table_)
				
				lateinit var scrollbarWidth: DoubleProperty
				
				// Top bar
				hbox(spacing = 2.0, alignment = Pos.CENTER) {
					addClass(GlobalStyles.tableHeader_)
					
					scrollbarWidth = paddingRightProperty
					label("") {
						addClass(GlobalStyles.tableItem_)
					}
					for(header in arrayListOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")) {
						label(header.translate(Language.TranslationTypes.Week)) {
							addClass(GlobalStyles.tableItem_)
							addClass(GlobalStyles.tableHeaderItem_)
							addClass(WeekStyles.tableTimeHeader_)
						}
					}
				}
				
				var table: ScrollPane? = null
				
				// table_
				fun updateTable() {
					children.remove(table)
					log("updated table_ view", LogType.LOW)
					var scrollToHour = 0
					
					table = scrollpane(fitToWidth = true, fitToHeight = true) {
						addClass(GlobalStyles.disableFocusDraw_)
						addClass(GlobalStyles.maxHeight_)
						addClass(GlobalStyles.background_)
						isPannable = true
						
						// update top bar fake scrollbar padding  (wait for width update,so that scrollbars were created already; and then update if scrollbar width changes[appears/disappears])
						widthProperty().listen(once = true) {
							lookupAll(".scroll-bar").filterIsInstance<ScrollBar>().filter { it.orientation == Orientation.VERTICAL }[0].let { bar ->
								bar.visibleProperty().listen { visible ->
									if(visible) {
										scrollbarWidth.value = 13.3 + 2 // 13.3 = scrollpane  scrollbarWidthInitial.toDouble() + 2 // 2 padding right of inner vbox
									} else {
										scrollbarWidth.value = 2.0 //scrollbarWidthInitial.toDouble() + 2 // 2 padding right of inner vbox
									}
								}
							}
						}
						
						hbox {
							addClass(GlobalStyles.background_)
							vbox(alignment = Pos.TOP_CENTER) {
								addClass(WeekStyles.tableDay_)
								for(hour in 0..23) {
									vbox(alignment = Pos.CENTER) {
										addClass(WeekStyles.TimeCell_)
										if(hour != 23) { // remove border on last element
											style(append = true) {
												borderColor += box(c(0.75, 0.75, 0.75))
												borderStyle += BorderStrokeStyle.DOTTED
												borderWidth += box(0.px, 0.px, 2.px, 0.px)
											}
										}
										
										if(now.hour == hour && week.time.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR) == now.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR) && week.time.year == now.year) addClass(
											WeekStyles.ActiveTimeCell_
										)
										
										label {
											style {
												fontSize = 20.px
											}
											@Suppress("ImplicitDefaultLocale")
											text = String.format("%02d", hour)
										}
									}
								}
							}
							for((dayOfWeek, day) in week.allDays) {
								val appointments = week.allDays[dayOfWeek]?.appointments ?: listOf()
								log("Day: $dayOfWeek: $appointments", LogType.LOW)
								
								vbox(alignment = Pos.TOP_CENTER) {
									addClass(WeekStyles.tableDay_)
									
									for(hour in 0..23) {
										// outer tableCell with border
										hbox {
											addClass(WeekStyles.TimeCell_)
											if(hour != 23)  // remove border on last element
												addClass(WeekStyles.TimeCellBorder_)
											
											if(now.hour == hour && week.time.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR) == now.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR) && week.time.year == now.year) {
												addClass(WeekStyles.ActiveTimeCell_)
												scrollToHour = hour
											}
											
											// inner tableCell
											hbox(spacing = 2.0, alignment = Pos.CENTER) {
												
												// appointments
												val cellAppointments = appointments.filter {
													val cellTimeStart = LocalDateTime.of(day.time.year, day.time.month, day.time.dayOfMonth, hour, 0)
													val cellTimeEnd = LocalDateTime.of(day.time.year, day.time.month, day.time.dayOfMonth, hour, 0).plusHours(1)
													cellTimeEnd > it.start.value && cellTimeStart < it.end.value
												}
												
												for((ind, app) in cellAppointments.withIndex()) {
													// colored box(es)
													hbox {
														gridpaneConstraints {
															columnRowIndex(ind, 0)
														}
														
														label(app.title)
														style {
															prefWidth = Int.MAX_VALUE.px
															
															// val height = ((app.start + app.duration) - hour * 60).coerceAtMost(60).toInt()
															
															// prefHeight = 20.px//height * (this@gridpane.height / 60).px
															// maxHeight_ = prefHeight
															// minHeight = prefHeight
															
															// translateY = -10.px
															
															padding = box(2.px)
															backgroundColor += app.type.value.color.value // TODO repaint
														}
													}
												}
												
												addClass(WeekStyles.UnHoveredInnerTimeCell_)
												onMouseEntered = EventHandler {
													addClass(WeekStyles.HoveredInnerTimeCell_)
												}
												onMouseExited = EventHandler {
													removeClass(WeekStyles.HoveredInnerTimeCell_)
												}
												
												contextmenu {
													item("new appointment".translate(Language.TranslationTypes.Week)) {
														action {
															AppointmentPopup.open("new appointment".translate(Language.TranslationTypes.AppointmentPopup),
																"create".translate(Language.TranslationTypes.AppointmentPopup),
																false,
																null,
																Timing.getNowUTC(week.time.year, week.time.month, day.time.dayOfMonth, hour),
																Timing.getNowUTC(week.time.year, week.time.month, day.time.dayOfMonth, hour).plusHours(1),
																save = { app: Appointment ->
																	log("Created:$app")
																	week.allDays[app.start.value.dayOfWeek]?.appointments?.add(app) // TODO replace this
																	updateTable()
																	updateCallback()
																})
														}
													}
													menu("remove appointment".translate(Language.TranslationTypes.Week)) {
														cellAppointments.forEach { appointment ->
															item(appointment.title) {
																action {
																	val remove = Alert(Alert.AlertType.CONFIRMATION).apply {
																		title = "remove?".translate(Language.TranslationTypes.Week)
																		headerText = "do you want to remove %s appointment".translate(Language.TranslationTypes.Week, appointment.title)
																	}.showAndWait().get()
																	if(remove.buttonData == ButtonBar.ButtonData.OK_DONE) {
																		log("Removed:$appointment") // TODO multi day
																		week.allDays[appointment.start.value.dayOfWeek]?.appointments?.remove(appointment) // TODO replace same here
																		appointment.remove()
																		updateTable()
																		updateCallback()
																	}
																}
															}
														}
													}
													menu("edit appointment".translate(Language.TranslationTypes.Week)) {
														cellAppointments.forEach { appointment ->
															item(appointment.title) {
																action {
																	AppointmentPopup.open("edit appointment".translate(Language.TranslationTypes.AppointmentPopup),
																		"save".translate(Language.TranslationTypes.AppointmentPopup),
																		false,
																		appointment,
																		Timing.getNowLocal(), // irrelevant, as they get overridden by values in appointment
																		Timing.getNowLocal(),
																		save = { app: Appointment ->
																			log("Updated:$app")
																			updateTable()
																		})
																}
															}
														}
													}
												}
											}
										}
									}
								}
							}
						}
						// scroll to current time a place ~in middle
						vvalue = (scrollToHour.toDouble() / 23) * vmax
					}
				}
				
				updateTable()
			}
		}
	}
}
