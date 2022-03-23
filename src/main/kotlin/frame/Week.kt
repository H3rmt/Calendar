package frame

import calendar.Appointment
import calendar.Day
import calendar.Timing
import calendar.Timing.UTCEpochMinuteToLocalDateTime
import calendar.Timing.toUTCEpochMinute
import calendar.Week
import calendar.getTypes
import calendar.now
import datetimepicker.DateTimePicker
import datetimepicker.dateTimePicker
import javafx.beans.property.*
import javafx.event.*
import javafx.geometry.*
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.paint.*
import javafx.scene.text.*
import javafx.stage.*
import listen
import logic.LogType
import logic.getLangString
import logic.log
import tornadofx.*
import java.time.LocalDateTime
import java.time.temporal.IsoFields



fun createWeekTab(pane: TabPane, week: Week, _day: Day?, updateCallback: () -> Unit): Tab {
	log("creating week tab", LogType.IMPORTANT)
	return pane.tab(week.date) {
		isClosable = true
		
		stackpane {
			style(append = true) {
				padding = box(6.px)
			}
			
			// mainTab
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
						text = "Test"
						
						addClass(Styles.Tabs.titleButtons)
					}
				}
				
				separate()
				
				log("creating table view", LogType.LOW)
				// Table view
				vbox(spacing = 1.0, alignment = Pos.TOP_CENTER) {
					addClass(Styles.CalendarTableView.table)
					addClass(Styles.disableFocusDraw)
					
					lateinit var topMargin: DoubleProperty
					
					// Top bar
					hbox(spacing = 2.0, alignment = Pos.CENTER) {
						padding = Insets(3.0)
						style {
							backgroundColor += Color.WHITE
							paddingRight = 15.3
						}
						topMargin = paddingRightProperty
						label("") {
							addClass(Styles.CalendarTableView.tableItem)
						}
						for(day in arrayListOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")) {
							label(day) {
								addClass(Styles.CalendarTableView.tableItem)
								addClass(Styles.WeekTab.tableTimeHeader)
								addClass(Styles.CalendarTableView.cellHeaderLabel)
							}
						}
					}
					
					var table: ScrollPane? = null
					
					// table
					fun updateTable() {
						children.remove(table)
						log("updated table view", LogType.LOW)
						var scrollToHour = 0
						
						table = scrollpane(fitToWidth = true) {
							
							// update top bar fake scrollbar padding  (wait for width update,so that scrollbars were created already; and then update if scrollbar width changes[appears/disappears])
							widthProperty().listen(once = true) {
								lookupAll(".scroll-bar").filterIsInstance<ScrollBar>().filter { it.orientation == Orientation.VERTICAL }[0].let { bar ->
									fun update(visible: Boolean) {
										if(visible) {
											topMargin.value = bar.width + 2 //+ scrollbarWidthInitial.toDouble() + 2 // 2 padding right of inner vbox
										} else {
											topMargin.value = 2.0 //scrollbarWidthInitial.toDouble() + 2 // 2 padding right of inner vbox
										}
									}
									bar.visibleProperty().listen { visible ->
										update(visible)
									}
								}
							}
							
							style {
								borderWidth += box(1.px)
								borderColor += box(Color.WHITE)
							}
							
							hbox(spacing = 2.0, alignment = Pos.CENTER) {
								style(append = true) {
									backgroundColor += Color.WHITE
								}
								vbox(alignment = Pos.TOP_CENTER) {
									addClass(Styles.WeekTab.tableDay)
									for(hour in 0..23) {
										vbox(alignment = Pos.CENTER) {
											addClass(Styles.WeekTab.TimeCell)
											if(hour != 23) { // remove border on last element
												style(append = true) {
													borderColor += box(c(0.75, 0.75, 0.75))
													borderStyle += BorderStrokeStyle.DOTTED
													borderWidth += box(0.px, 0.px, 2.px, 0.px)
												}
											}
											
											if(now.hour == hour && week.time.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR) == now.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR) && week.time.year == now.year)
												addClass(Styles.WeekTab.ActiveTimeCell)
											
											label {
												style {
													fontSize = 20.px
												}
												text = String.format("%02d", hour)
											}
										}
									}
								}
								for((dayOfWeek, day) in week.allDays) {
									val appointments = week.allDays[dayOfWeek]?.appointments ?: listOf()
									log("Day: $dayOfWeek: $appointments", LogType.LOW)
									
									vbox(alignment = Pos.TOP_CENTER) {
										addClass(Styles.WeekTab.tableDay)
										
										for(hour in 0..23) {
											// outer cell with border
											hbox {
												addClass(Styles.WeekTab.TimeCell)
												if(hour != 23)  // remove border on last element
													addClass(Styles.WeekTab.TimeCellBorder)
												
												if(now.hour == hour && week.time.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR) == now.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR) && week.time.year == now.year) {
													addClass(Styles.WeekTab.ActiveTimeCell)
													scrollToHour = hour
												}
												
												// inner cell
												hbox {
													style(append = true) {
														alignment = Pos.CENTER
														spacing = 2.px
														padding = box(0.px)
													}
													// appointments
													val cellAppointments = appointments.filter {
														val from = LocalDateTime.of(day.time.year, day.time.month, day.time.dayOfMonth, hour, 0).toUTCEpochMinute()
														from < it.start + it.duration && from + 60 > it.start
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
																// maxHeight = prefHeight
																// minHeight = prefHeight
																
																// translateY = -10.px
																
																padding = box(2.px)
																backgroundColor += app.type.color
															}
														}
													}
													
													addClass(Styles.WeekTab.UnHoveredInnerTimeCell)
													onMouseEntered = EventHandler {
														addClass(Styles.WeekTab.HoveredInnerTimeCell)
													}
													onMouseExited = EventHandler {
														removeClass(Styles.WeekTab.HoveredInnerTimeCell)
													}
													
													contextmenu {
														item(getLangString("new appointment")) {
															action {
																NewAppointmentPopup.open(getLangString("new appointment"), getLangString("Create"),
																	false,
																	null,
																	Timing.getNowUTC(week.time.year, week.time.month, day.time.dayOfMonth, hour),
																	Timing.getNowUTC(week.time.year, week.time.month, day.time.dayOfMonth, hour + 1),
																	save = { app: Appointment ->
																		log("Created:$app")
																		week.allDays[UTCEpochMinuteToLocalDateTime(app.start).dayOfWeek]?.appointments?.add(app)
																		updateTable()
																		updateCallback()
																	}
																)
															}
														}
														menu(getLangString("remove appointment")) {
															cellAppointments.forEach { appointment ->
																item(appointment.title) {
																	action {
																		val remove = Alert(Alert.AlertType.CONFIRMATION).apply {
																			title = getLangString("remove?")
																			headerText = getLangString("do you want to remove appointment for %s") // TODO replace %s with title
																		}.showAndWait().get()
																		if(remove.buttonData == ButtonBar.ButtonData.OK_DONE) {
																			log("Removed:$appointment") // TODO multi day
																			week.allDays[UTCEpochMinuteToLocalDateTime(appointment.start).dayOfWeek]?.appointments?.remove(appointment)
																			updateTable()
																			updateCallback()
																		}
																	}
																}
															}
														}
														menu(getLangString("edit appointment")) {
															cellAppointments.forEach { appointment ->
																item(appointment.title) {
																	action {
																		NewAppointmentPopup.open(getLangString("edit appointment"), getLangString("Save"),
																			false,
																			appointment,
																			Timing.getNowLocal(), // irrelevant, as they get overridden by values in appointment
																			Timing.getNowLocal(),
																			save = { app: Appointment ->
																				
																				updateTable()
																				updateCallback()
																			}
																		)
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
			
			// used to shadow the overflow from tab
			pane {
				isMouseTransparent = true
				addClass(Styles.Tabs.shadowBorder)
			}
		}
	}
}

class NewAppointmentPopup: Fragment() {
	override val scope = super.scope as ItemsScope
	
	private var appointment: Appointment? = scope.appointment
	
	// do not bind directly, instead copy values into new Observables, to only save an updateAppointment()
	private var start: Property<LocalDateTime> = (appointment?.start?.let { UTCEpochMinuteToLocalDateTime(it) } ?: scope.start).toProperty()
	private var end: Property<LocalDateTime> = (appointment?.let { UTCEpochMinuteToLocalDateTime(it.start + it.duration) } ?: scope.end).toProperty()
	private var appointmentTitle: Property<String> = (appointment?.title ?: "").toProperty()
	private var description: Property<String> = (appointment?.description ?: "").toProperty()
	private var type: Property<String> = (appointment?.type?.name ?: "").toProperty()
	
	private var onSave: (Appointment) -> Unit = scope.save
	
	private var error: Property<String> = "".toProperty()
	
	private lateinit var startPicker: DateTimePicker
	private lateinit var endPicker: DateTimePicker
	
	private var windowTitle: String = scope.title
	private var saveTitle: String = scope.savetitle
	
	private fun updateAppointment() {
		appointment?.let { app ->
			app.title = appointmentTitle.value
			app.description = description.value
			app.start = start.value.toUTCEpochMinute()
			app.duration = end.value.toUTCEpochMinute() - start.value.toUTCEpochMinute()
			app.type = getTypes().find { it.name == type.value }!!
		}
	}
	
	private fun createAppointment(): Appointment = Appointment.new(
		start.value.toUTCEpochMinute(),
		end.value.toUTCEpochMinute() - start.value.toUTCEpochMinute(),
		appointmentTitle.value,
		description.value,
		getTypes().find { it.name == type.value }!!,
		false
	)
	
	private fun checkAppointment(): String? {
		if(type.value == "") {
			return getLangString("missing type")
		} else if(appointmentTitle.value.isEmpty()) {
			return getLangString("missing title")
		} else if(end.value.toUTCEpochMinute() < start.value.toUTCEpochMinute()) {
			return getLangString("start must be before end")
		}
		return null
	}
	
	override fun onBeforeShow() {
		modalStage?.height = 320.0
		modalStage?.width = 400.0
	}
	
	override val root = form {
		style {
			backgroundColor += Color.WHITE
		}
		fieldset(getLangString(windowTitle)) {
			style {
				prefHeight = Int.MAX_VALUE.px
			}
			field("Type") {
				combobox(values = getTypes().map { it.name }, property = type)
			}
			field(getLangString("start to end")) {
				startPicker = dateTimePicker(dateTime = start)
				endPicker = dateTimePicker(dateTime = end)
			}
			field(getLangString("title")) {
				textfield(appointmentTitle)
			}
			field(getLangString("description")) {
				style(append = true) {
					prefHeight = Int.MAX_VALUE.px
					minHeight = 60.px
					padding = box(0.px, 0.px, 20.px, 0.px)
				}
				textarea(description) {
					style(append = true) {
						prefHeight = Int.MAX_VALUE.px
					}
				}
			}
			
			buttonbar {
				textfield(error) {
					style(append = true) {
						backgroundColor += Color.TRANSPARENT
						borderStyle += BorderStrokeStyle.NONE
						textFill = Color.RED
						fontSize = 120.percent
						fontWeight = FontWeight.BOLD
					}
				}
				button(getLangString("Cancel")) {
					isCancelButton = true
					action {
						close()
					}
				}
				button(saveTitle) {
					isDefaultButton = true
					action {
						val check = checkAppointment()
						if(check == null) {
							if(appointment == null)
								appointment = createAppointment()
							updateAppointment()
							onSave.invoke(appointment!!)
							close()
						} else {
							error.value = check
						}
					}
				}
			}
		}
	}
	
	class ItemsScope(val title: String, val savetitle: String, val appointment: Appointment?, val start: LocalDateTime, val end: LocalDateTime, val save: (Appointment) -> Unit):
		Scope()
	
	companion object {
		fun open(title: String, saveTitle: String, block: Boolean, appointment: Appointment?, start: LocalDateTime, end: LocalDateTime, save: (Appointment) -> Unit): Stage? {
			val scope = ItemsScope(title, saveTitle, appointment, start, end, save)
			return find<NewAppointmentPopup>(scope).openModal(modality = if(block) Modality.APPLICATION_MODAL else Modality.NONE, escapeClosesWindow = false)
		}
	}
	
}