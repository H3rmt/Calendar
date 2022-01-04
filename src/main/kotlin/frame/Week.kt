package frame

import calendar.Appointment
import calendar.Day
import calendar.Timing
import calendar.Timing.UTCEpochMinuteToLocalDateTime
import calendar.Timing.toUTCEpochMinute
import calendar.Types
import calendar.Week
import calendar.now
import calendar.saveDayAppointment
import datetimepicker.DateTimePicker
import datetimepicker.dateTimePicker
import javafx.beans.property.*
import javafx.event.*
import javafx.geometry.*
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.paint.*
import javafx.stage.*
import listen
import logic.LogType
import logic.getLangString
import logic.log
import tornadofx.*
import java.time.LocalDateTime
import java.time.temporal.IsoFields
import kotlin.math.min


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
						label("Monday") {
							addClass(Styles.CalendarTableView.tableItem)
							addClass(Styles.WeekTab.tableTimeHeader)
							addClass(Styles.CalendarTableView.cellHeaderLabel)
						}
						label("Tuesday") {
							addClass(Styles.CalendarTableView.tableItem)
							addClass(Styles.WeekTab.tableTimeHeader)
							addClass(Styles.CalendarTableView.cellHeaderLabel)
						}
						label("Wednesday") {
							addClass(Styles.CalendarTableView.tableItem)
							addClass(Styles.WeekTab.tableTimeHeader)
							addClass(Styles.CalendarTableView.cellHeaderLabel)
						}
						label("Thursday") {
							addClass(Styles.CalendarTableView.tableItem)
							addClass(Styles.WeekTab.tableTimeHeader)
							addClass(Styles.CalendarTableView.cellHeaderLabel)
						}
						label("Friday") {
							addClass(Styles.CalendarTableView.tableItem)
							addClass(Styles.WeekTab.tableTimeHeader)
							addClass(Styles.CalendarTableView.cellHeaderLabel)
						}
						label("Saturday") {
							addClass(Styles.CalendarTableView.tableItem)
							addClass(Styles.WeekTab.tableTimeHeader)
							addClass(Styles.CalendarTableView.cellHeaderLabel)
						}
						label("Sunday") {
							addClass(Styles.CalendarTableView.tableItem)
							addClass(Styles.WeekTab.tableTimeHeader)
							addClass(Styles.CalendarTableView.cellHeaderLabel)
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
												text = String.format("%02d:00", hour)
											}
										}
									}
								}
								for((dayOfWeek, day) in week.allDays) {
									val appointments = week.allDays[dayOfWeek]?.appointments ?: listOf()
									log("Day: $dayOfWeek: $appointments")
									
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
													val cellappointments = appointments.filter {
														it.daystart < (hour + 1) * 60 && (it.daystart + it.duration) > hour * 60
													}
													
													for((ind, app) in cellappointments.withIndex()) {
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
																NewAppointmentPopup.open(
																	false,
																	Timing.getNowUTC(week.time.year, week.time.month, day.time.dayOfMonth, hour),
																	Timing.getNowUTC(week.time.year, week.time.month, day.time.dayOfMonth, hour + 1)
																) { app: Appointment ->
																	log("Created:$app")
																	saveDayAppointment(app)
																	updateCallback()
																	week.allDays[UTCEpochMinuteToLocalDateTime(app.start).dayOfWeek]?.appointments?.add(app)
																	updateTable()
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
							//vvalue = (min(scrollToHour.toDouble() + 8, 24.0) / 24.0) * vmax
							vvalue = (min(0.toDouble(), 24.0) / 24.0) * vmax
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
	
	var start: Property<LocalDateTime> = scope.start.toProperty()
	var end: Property<LocalDateTime> = scope.end.toProperty()
	private var appointmentTitle: Property<String> = "".toProperty()
	private var description: Property<String> = "".toProperty()
	private var type: Property<Types?> = SimpleObjectProperty<Types>(null)
	
	private var savecall: (Appointment) -> Unit = scope.save
	
	private lateinit var startpicker: DateTimePicker
	private lateinit var endpicker: DateTimePicker
	
	private fun createAppointment(): Appointment {
		val _start: Long = start.value.toUTCEpochMinute()
		val _duration: Long = end.value.toUTCEpochMinute() - _start  // subtract start from duration
		val _title: String = appointmentTitle.value
		val _description: String = description.value
		val _type: Types = type.value!!
		return Appointment(_start, _duration, _title, _description, _type)
	}
	
	override fun onBeforeShow() {
		modalStage?.height = 320.0
		modalStage?.width = 400.0
	}
	
	override val root = form {
		style {
			backgroundColor += Color.WHITE
		}
		fieldset(getLangString("New appointment")) {
			style {
				prefHeight = Int.MAX_VALUE.px
			}
			field("Type") {
				combobox(values = Types.clonetypes(), property = type)
			}
			field(getLangString("start to end")) {
				startpicker = dateTimePicker(dateTime = start)
				endpicker = dateTimePicker(dateTime = end)
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
				button(getLangString("Cancel")) {
					isCancelButton = true
					action {
						close()
					}
				}
				button(getLangString("Create")) {
					isDefaultButton = true
					action {
						savecall.invoke(createAppointment())
						close()
					}
				}
			}
		}
	}
	
	class ItemsScope(val start: LocalDateTime, val end: LocalDateTime, val save: (Appointment) -> Unit): Scope()
	
	companion object {
		fun open(block: Boolean, start: LocalDateTime, end: LocalDateTime, save: (Appointment) -> Unit): Stage? {
			val scope = ItemsScope(start, end, save)
			return find<NewAppointmentPopup>(scope).openModal(modality = if(block) Modality.APPLICATION_MODAL else Modality.NONE, escapeClosesWindow = false)
		}
	}
	
}