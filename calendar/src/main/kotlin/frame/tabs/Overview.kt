package frame.tabs

import calendar.Appointment
import calendar.Appointments
import calendar.Note
import calendar.Notes
import calendar.Timing
import calendar.Type
import frame.TabManager
import frame.adjustWidth
import frame.createFXImage
import frame.popup.ReminderPopup
import frame.styles.GlobalStyles
import frame.styles.OverviewStyles
import frame.styles.TabStyles
import javafx.beans.property.*
import javafx.event.*
import javafx.geometry.*
import javafx.scene.control.*
import javafx.scene.image.*
import logic.Language
import logic.LogType
import logic.listen
import logic.log
import logic.translate
import tornadofx.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoField
import java.time.temporal.IsoFields

val overviewTime: Property<LocalDate> = Timing.getNow().toLocalDate().toProperty()

fun createOverviewTab(pane: TabPane): Tab {
	log("creating overview tab")
	return pane.tab("calender".translate(Language.TranslationTypes.Overview)) {
		isClosable = false
		addClass(TabStyles.tab_)

		vbox {
			addClass(TabStyles.content_)
			// Top bar
			hbox(spacing = 20.0, alignment = Pos.CENTER) {
				addClass(TabStyles.topbar_)
				button("<") {
					addClass(TabStyles.titleButton_)
					action {
						overviewTime.value = overviewTime.value.plusMonths(-1)
					}
				}
				label("") { // gets updated later
					addClass(TabStyles.title_)
					minWidth = 200.0
					alignment = Pos.CENTER
					overviewTime.listen(runOnce = true) {
						this.text = it.month.name
					}
				}
				button(">") {
					addClass(TabStyles.titleButton_)
					action {
						overviewTime.value = overviewTime.value.plusMonths(1)
					}
				}
			}

			// Table view
			vbox(spacing = 1.0, alignment = Pos.TOP_CENTER) {
				addClass(GlobalStyles.disableFocusDraw_)
				addClass(GlobalStyles.table_)

				lateinit var scrollbarWidth: DoubleProperty

				// Top bar
				hbox(spacing = 5.0, alignment = Pos.CENTER) {
					addClass(GlobalStyles.tableHeader_)

					scrollbarWidth = paddingRightProperty
					label("") {
						addClass(GlobalStyles.tableItem_)
						style {
							minWidth = 85.px // TODO get from cell (split cell styles)
						}
					}
					for(header in arrayListOf(
						"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
					)) {
						label(header.translate(Language.TranslationTypes.Note)) {
							addClass(GlobalStyles.tableItem_)
							addClass(GlobalStyles.tableHeaderItem_)
							addClass(OverviewStyles.headerItem_)
						}
					}
				}

				scrollpane(fitToWidth = true, fitToHeight = true) {
					addClass(GlobalStyles.disableFocusDraw_)
					addClass(GlobalStyles.maxHeight_)
					addClass(GlobalStyles.background_)
					isPannable = true

					// update top bar fake scrollbar padding  (wait for width update,so that scrollbars were created already; and then update if scrollbar width changes[appears/disappears])
					adjustWidth(scrollbarWidth)

					// gets stretched across whole scrollpane
					vbox(spacing = 5.0, alignment = Pos.TOP_CENTER) {
						addClass(GlobalStyles.background_)

						val update: (LocalDate) -> Unit = { new: LocalDate ->
							clear()
							// get date of first visible cell
							var time: LocalDate = new.withDayOfMonth(1).with(ChronoField.DAY_OF_WEEK, 1)
							// loop until last week with day in this month is complete
							do {
								// clone time, so that later called callbacks use right time and not time of last day
								val ctime = time

								// week box
								hbox(spacing = 5.0, alignment = Pos.CENTER) {
									// week cell
									vbox {
										addClass(GlobalStyles.tableItem_)
										addClass(OverviewStyles.cell_)
										addClass(OverviewStyles.disabledCell_)
										gridpane {
											style {
												prefWidth = Int.MAX_VALUE.px
												padding = box(0.px, 3.px, 2.px, 3.px)
											}
											anchorpane {
												gridpaneConstraints {
													columnIndex = 0
												}
											}
											label(ctime.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR).toString()) {
												gridpaneConstraints {
													columnIndex = 1
												}
												addClass(OverviewStyles.cellLabel_)
											}
											anchorpane {
												gridpaneConstraints {
													columnIndex = 2
												}

												val imgView = imageview {
													addClass(OverviewStyles.cellIcon_)
													fitHeight = 21.5
													fitWidth = 21.5
												}

												// update images on notes updates
												lateinit var img: Image
												lateinit var hoveredImg: Image
												val update = { list: List<Note> ->
													img = if(list.isEmpty()) {
														createFXImage("note.svg")
													} else {
														createFXImage("note active.svg")
													}
													hoveredImg = if(list.isEmpty()) {
														createFXImage("note hovered.svg")
													} else {
														createFXImage("note active hovered.svg")
													}
													imgView.image = if(imgView.isHover)
														hoveredImg else img
												}
												Notes.getNotesAt(ctime).listen(update, runOnce = true)

												onMouseClicked
												onMouseEntered = EventHandler { imgView.image = hoveredImg }
												onMouseExited = EventHandler { imgView.image = img }
											}
										}

										vbox(spacing = 1, alignment = Pos.TOP_LEFT) {
											// just rerender whole day on change (doesn't make a difference here,
											// to append to pane on add and remove on remove)
											val update: (List<Appointment>) -> Unit = { appointments: List<Appointment> ->
												clear()
												val width =
													(if(pane.width.toInt() % 2 == 0) pane.width.toInt() else pane.width.toInt() + 1).toDouble()

												val list = mutableMapOf<Type, Int>()
												for(appointment in appointments) {
													list[appointment.type.value] = list[appointment.type.value]?.plus(1) ?: 1
												}

												for((index, typeCount) in list.entries.withIndex()) {
													label {
														typeCount.key.name.listen(runOnce = true) {
															text = "$it: ${typeCount.value}"
														}
														typeCount.key.color.listen(runOnce = true) {
															textFill = it
														}

														addClass(OverviewStyles.cellAppointLabel_)
														maxWidth = width - HORIZONTAL_LEFT_MARGIN - CIRCLE_WIDTH
														ellipsisString = ".."
														textOverrun = OverrunStyle.ELLIPSIS
													}
												}
											}

											Appointments.getAppointmentsFromTo(
												ctime.atStartOfDay(), ctime.plusWeeks(1).atStartOfDay(), ctime.dayOfWeek
											).listen(update, runOnce = true)
										}
									}

									// day cells
									do {
										// clone time, so that later called callbacks use right time and not time of last day
										val cctime = time
										vbox(alignment = Pos.TOP_LEFT) {
											addClass(GlobalStyles.tableItem_)
											addClass(OverviewStyles.cell_)
											if(cctime.month != new.month)
												addClass(OverviewStyles.disabledCell_)
											gridpane {
												style {
													prefWidth = Int.MAX_VALUE.px
													padding = box(0.px, 3.px, 2.px, 3.px)
												}
												anchorpane {
													gridpaneConstraints {
														columnIndex = 0
													}
													val defaultImg = createFXImage("remind.svg")
													val hoveredImg = createFXImage("remind hovered.svg")

													val img = imageview(defaultImg) {
														addClass(OverviewStyles.cellIcon_)
														fitHeight = 21.5
														fitWidth = 21.5
													}
													onMouseClicked = EventHandler {
														it.consume()
														ReminderPopup.openNew(cctime.atStartOfDay(), null)
													}
													onMouseEntered = EventHandler { img.image = hoveredImg }
													onMouseExited = EventHandler { img.image = defaultImg }
												}
												label(cctime.dayOfMonth.toString()) {
													gridpaneConstraints {
														columnIndex = 1
													}
													addClass(OverviewStyles.cellLabel_)
												}
												anchorpane {
													gridpaneConstraints {
														columnIndex = 2
													}

													val imgView = imageview {
														addClass(OverviewStyles.cellIcon_)
														fitHeight = 21.5
														fitWidth = 21.5
													}

													// update images on notes updates
													lateinit var img: Image
													lateinit var hoveredImg: Image
													val update = { list: List<Note> ->
														img = if(list.isEmpty()) {
															createFXImage("note.svg")
														} else {
															createFXImage("note active.svg")
														}
														hoveredImg = if(list.isEmpty()) {
															createFXImage("note hovered.svg")
														} else {
															createFXImage("note active hovered.svg")
														}
														imgView.image = if(imgView.isHover)
															hoveredImg else img
													}

													Notes.getNotesAt(cctime).listen(update, runOnce = true)

													onMouseClicked = EventHandler {
														it.consume()
														TabManager.openTab(
															"WeekNotes/${cctime.year}/${cctime.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)}/${cctime.dayOfWeek}",
															::createNoteTab,
															cctime,
															false
														)
													}
													onMouseEntered = EventHandler { imgView.image = hoveredImg }
													onMouseExited = EventHandler { imgView.image = img }
												}
											}
											vbox {
												// just rerender whole day on change (doesn't make a difference here,
												// to append to pane on add and remove on remove)
												val update = { appointments: List<Appointment> ->
													clear()
													val width =
														(if(pane.width.toInt() % 2 == 0) pane.width.toInt() else pane.width.toInt() + 1).toDouble()

													for((index, appointment) in appointments.withIndex()) {
														label {
															appointment.title.listen(runOnce = true) {
																text = it
															}
															appointment.type.value.color.listen(runOnce = true) {
																textFill = it
															}

															addClass(OverviewStyles.cellAppointLabel_)
															maxWidth = width - HORIZONTAL_LEFT_MARGIN - CIRCLE_WIDTH
															ellipsisString = ".."
															textOverrun = OverrunStyle.ELLIPSIS
														}
													}
												}

												Appointments.getAppointmentsFromTo(
													cctime.atStartOfDay(), cctime.plusDays(1).atStartOfDay(), cctime.dayOfWeek
												).listen(update, runOnce = true)
											}
										}
										time = time.plusDays(1)
									} while(time.dayOfWeek != DayOfWeek.MONDAY)
									onMouseClicked = EventHandler {
										if(it.clickCount == 2) {
											it.consume()
											TabManager.openTab(
												"Week/${ctime.year}/${ctime.month}/${ctime.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)}",
												::createWeekTab,
												ctime
											)
										}
									}
								}
							} while(time.month == new.month)
						}
						overviewTime.listen(update, runOnce = true)
					}
				}
			}
		}
		log("created overview tab")
	}
}

const val CIRCLE_WIDTH = 8.0
const val HORIZONTAL_LEFT_MARGIN = 8.0
