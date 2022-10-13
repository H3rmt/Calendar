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
import javafx.scene.layout.*
import javafx.scene.paint.*
import logic.Language
import logic.ObservableListListeners.listen
import logic.ObservableValueListeners.listen
import logic.log
import logic.translate
import tornadofx.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoField
import java.time.temporal.IsoFields


/**
 * Create Overview tab
 *
 * Never called directly (called by
 * TabManager.openTab(...,::createOverviewTab, ... ) )
 *
 * @param pane ref to main pane
 */
fun createOverviewTab(pane: TabPane): Tab {
	log("creating overview tab")
	val overviewTime: Property<LocalDate> = Timing.getNow().toLocalDate().toProperty()

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
					overviewTime.listen(runOnce = true) { it: LocalDate ->
						this.text = it.month.name.translate(Language.TranslationTypes.Global)
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
						"monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"
					)) {
						label(header.translate(Language.TranslationTypes.Global)) {
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
								// week box
								weekCell(this, pane, time, new)
								time = time.plusDays(7)
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

/**
 * creates a week cell
 *
 * @param vBox ref to parent
 * @param pane ref to pane
 * @param ctime currentTime
 * @param date date for appointment and notes to display
 */
private fun weekCell(vBox: VBox, pane: TabPane, ctime: LocalDate, date: LocalDate) {
	vBox.hbox(spacing = 5.0, alignment = Pos.CENTER) {
		var timeCounter = ctime
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

					onMouseClicked = EventHandler {
						it.consume()
						TabManager.openTab(
							"WeekNotes/${ctime.year}/${ctime.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)}/${ctime.dayOfWeek}",
							::createNoteTab,
							ctime,
							true
						)
					}
					onMouseEntered = EventHandler { imgView.image = hoveredImg }
					onMouseExited = EventHandler { imgView.image = img }
				}
			}

			// appointment list view
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

					for((key, value) in list) {
						label {
							key.name.listen(runOnce = true) { it: String ->
								text = "$it: $value"
							}
							key.color.listen(runOnce = true) { it: Color ->
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
			val time = timeCounter

			vbox(alignment = Pos.TOP_LEFT) {
				addClass(GlobalStyles.tableItem_)
				addClass(OverviewStyles.cell_)
				if(time.month != date.month)
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
							ReminderPopup.openNew(time.atStartOfDay(), null)
						}
						onMouseEntered = EventHandler { img.image = hoveredImg }
						onMouseExited = EventHandler { img.image = defaultImg }
					}
					label(time.dayOfMonth.toString()) {
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

						Notes.getNotesAt(time).listen(update, runOnce = true)

						onMouseClicked = EventHandler {
							it.consume()
							TabManager.openTab(
								"WeekNotes/${time.year}/${time.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)}/${time.dayOfWeek}",
								::createNoteTab,
								time,
								false
							)
						}
						onMouseEntered = EventHandler { imgView.image = hoveredImg }
						onMouseExited = EventHandler { imgView.image = img }
					}
				}

				// appointment list view
				vbox {
					// just rerender whole day on change (doesn't make a difference here,
					// to append to pane on add and remove on remove)
					val update = { appointments: List<Appointment> ->
						clear()
						val width =
							(if(pane.width.toInt() % 2 == 0) pane.width.toInt() else pane.width.toInt() + 1).toDouble()

						for(appointment in appointments) {
							label {
								appointment.title.listen(runOnce = true) { it: String ->
									text = it
								}
								appointment.type.value.color.listen(runOnce = true) { it: Color ->
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
						time.atStartOfDay(), time.plusDays(1).atStartOfDay(), time.dayOfWeek
					).listen(update, runOnce = true)
				}
			}
			timeCounter = timeCounter.plusDays(1)
		} while(timeCounter.dayOfWeek != DayOfWeek.MONDAY)

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
}

const val CIRCLE_WIDTH = 8.0
const val HORIZONTAL_LEFT_MARGIN = 8.0
