package frame.tabs

import calendar.Appointment
import calendar.Appointments
import calendar.Timing
import frame.adjustWidth
import frame.styles.GlobalStyles
import frame.styles.TabStyles
import frame.styles.WeekStyles
import javafx.beans.property.*
import javafx.event.*
import javafx.geometry.*
import javafx.scene.control.*
import javafx.scene.layout.*
import logic.Language
import logic.ObservableListListeners.listen
import logic.log
import logic.translate
import tornadofx.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.IsoFields


fun createWeekTab(pane: TabPane, time: LocalDate): Tab {
	log("creating week tab")
	return pane.tab("") {
		text = "week %s - %s".translate(
			Language.TranslationTypes.Week,
			"${time.dayOfMonth}.${time.month.value}.",
			"${time.plusDays(6).dayOfMonth}.${time.plusDays(6).month.value}."
		)
		isClosable = true
		addClass(TabStyles.tab_)

		// mainTab
		vbox {
			addClass(TabStyles.content_)
			hbox(spacing = 40.0, alignment = Pos.CENTER) {
				addClass(TabStyles.topbar_)
				label("week".translate(Language.TranslationTypes.Week)) {
					addClass(TabStyles.title_)
					alignment = Pos.CENTER
				}
			}

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
					for(header in arrayListOf(
						"monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"
					)) {
						label(header.translate(Language.TranslationTypes.Global)) {
							addClass(GlobalStyles.tableItem_)
							addClass(GlobalStyles.tableHeaderItem_)
							addClass(WeekStyles.tableTimeHeader_)
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
					hbox {
						addClass(GlobalStyles.background_)
						// week number columns
						vbox {
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

									if(Timing.getNow().hour == hour && time.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR) == Timing.getNow()
											.get(
												IsoFields.WEEK_OF_WEEK_BASED_YEAR
											) && time.year == Timing.getNow().year
									) addClass(
										WeekStyles.ActiveTimeCell_
									)

									label {
										style {
											fontSize = 20.px
										}
										text = String.format("%02d", hour)
									}
								}
							}
						}

						for(day in 0..6) {
							val ctime = time.plusDays(day.toLong())
							vbox {
								addClass(WeekStyles.tableDay_)

								val update = { list: List<Appointment> ->
									clear()
									for(hour in 0..23) {
										val cctime = LocalDateTime.of(ctime, LocalTime.of(hour, 0))
										vbox(alignment = Pos.CENTER) {
											addClass(WeekStyles.TimeCell_)

											hbox(alignment = Pos.CENTER) {
												addClass(WeekStyles.UnHoveredInnerTimeCell_)
												onMouseEntered = EventHandler {
													addClass(WeekStyles.HoveredInnerTimeCell_)
												}
												onMouseExited = EventHandler {
													removeClass(WeekStyles.HoveredInnerTimeCell_)
												}

												// TODO Week appointments
												// do some filtering if appointment is at this day
												val f = list.filter {
													(!it.week.value && (it.start.value <= cctime.plusHours(1) && it.end.value >= cctime))
												}

												for((ind, app) in f.withIndex()) {
													// colored box(es)
													hbox {
														gridpaneConstraints {
															columnRowIndex(ind, 0)
														}

														label(app.title)
														style {
															prefWidth = Int.MAX_VALUE.px

															padding = box(2.px)
															backgroundColor += app.type.value.color.value // TODO bind
														}
													}
												}
											}
										}
									}
								}

								// reduces active listeners from 1 per hour of each day to one per day
								Appointments.getAppointmentsFromTo(
									ctime.atStartOfDay(), ctime.plusDays(1).atStartOfDay(), ctime.dayOfWeek
								).listen(update, runOnce = true)
							}
						}
					}
				}
			}
		}
		log("created week tab")
	}
}
