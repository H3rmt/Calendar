package frame.tabs

import CellDisplay
import Day
import Week
import calendar.*
import frame.TabManager
import frame.adjustWidth
import frame.createFXImage
import frame.popup.ReminderPopup
import frame.styles.GlobalStyles
import frame.styles.OverviewStyles
import frame.styles.TabStyles
import javafx.animation.*
import javafx.beans.property.DoubleProperty
import javafx.beans.property.Property
import javafx.beans.property.SimpleDoubleProperty
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.OverrunStyle
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.image.Image
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.shape.Circle
import javafx.scene.shape.CubicCurveTo
import javafx.scene.shape.MoveTo
import javafx.scene.shape.Path
import javafx.scene.text.FontWeight
import javafx.util.Duration
import logic.*
import tornadofx.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoField
import java.time.temporal.IsoFields

val overviewTime: Property<LocalDate> = Timing.getNow().toLocalDate().toProperty()

fun createOverviewTab(pane: TabPane): Tab {
	log("creating overview tab", LogType.IMPORTANT)
	return pane.tab("calender".translate(Language.TranslationTypes.Overview)) {
		isClosable = false
		addClass(TabStyles.tab_)
		
		vbox {
			addClass(TabStyles.content_)
			log("creating top bar", LogType.LOW)
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
			
			log("creating table view", LogType.LOW)
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
											style(append = true) {
//												backgroundColor += Color.AQUA
//												prefHeight = DETAILSPANMINHEIGHT.px
											}
											
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
//															translateX = HORIZONTAL_LEFT_MARGIN + CIRCLE_WIDTH
//															translateY = SIDE_TOP_MARGIN + index * (SPACING + CIRCLE_WIDTH) - CIRCLE_WIDTH / 1.1
														
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
													onMouseClicked
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
												style(append = true) {
//													backgroundColor += Color.AQUA
//													prefHeight = DETAILSPANMINHEIGHT.px
												}
												
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
//															translateX = HORIZONTAL_LEFT_MARGIN + CIRCLE_WIDTH
//															translateY = SIDE_TOP_MARGIN + index * (SPACING + CIRCLE_WIDTH) - CIRCLE_WIDTH / 1.1
															
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
						/*
						for((index, week) in list.withIndex()) {
							hbox(spacing = 5.0, alignment = Pos.CENTER) {
								val openTimeline = Timeline()
								val closeTimeline = Timeline()

								val cells = mutableListOf<VBox>()

								val expand = SimpleDoubleProperty(DETAILSPANMINHEIGHT.toDouble())

								val openAppointmentOpenAnimations: MutableList<MutableList<Animation>> = mutableListOf()
								val closeAppointmentOpenAnimations: MutableList<MutableList<Animation>> = mutableListOf()

								val temp = createCellGraphics(week, this@hbox, openTimeline, closeTimeline, expand)
								cells.add(temp[0] as VBox)
								@Suppress("UNCHECKED_CAST")
								openAppointmentOpenAnimations.add(temp[1] as MutableList<Animation>)
								@Suppress("UNCHECKED_CAST")
								closeAppointmentOpenAnimations.add(temp[2] as MutableList<Animation>)

								week.allDays.values.forEach {
									val graphic = createCellGraphics(it, this@hbox, openTimeline, closeTimeline, expand)
									cells.add(graphic[0] as VBox)

									if(it.time.dayOfYear == now.dayOfYear && it.time.year == now.year)
										(graphic[0] as VBox).addClass(OverviewStyles.markedCell_)

									@Suppress("UNCHECKED_CAST")
									openAppointmentOpenAnimations.add(graphic[1] as MutableList<Animation>)
									@Suppress("UNCHECKED_CAST")
									closeAppointmentOpenAnimations.add(graphic[2] as MutableList<Animation>)
								}


								val hoveredCell = SimpleIntegerProperty(-1)

								for((cellIndex, cell) in cells.withIndex()) {
									cell.onMouseEntered = EventHandler {
										cell.addClass(OverviewStyles.hoveredCell_)
										hoveredCell.value = cellIndex
									}
									cell.onMouseExited = EventHandler {
										cell.removeClass(OverviewStyles.hoveredCell_)
										hoveredCell.value = -1
									}
									cell.widthProperty().listen { selectedIndex.value = -2 /*-1 doesn't close -2 forces close of row*/ }
								}

								var openPreparation: Boolean

								onMouseEntered = EventHandler {
									if(selectedIndex.value != index) {
										openPreparation = true
										runAsync {
											Thread.sleep(getConfig<Double>(Configs.AnimationDelay).toLong())
											if(openPreparation) {
												openPreparation = false
												val skip = closeTimeline.totalDuration - closeTimeline.currentTime

												closeAppointmentOpenAnimations.forEach { ain -> ain.forEach { it.stop() } }
												closeTimeline.stop()
												openAppointmentOpenAnimations.forEach { ani -> ani.forEach { it.playFrom(skip) } }
												openTimeline.playFrom(skip)
											}
										}
									}
								}

								onMouseExited = EventHandler {
									if(selectedIndex.value != index) {
										openPreparation = false
										val skip = openTimeline.totalDuration - openTimeline.currentTime

										openAppointmentOpenAnimations.forEach { ain -> ain.forEach { it.stop() } }
										openTimeline.stop()
										closeAppointmentOpenAnimations.forEach { ani -> ani.forEach { it.playFrom(skip) } }
										closeTimeline.playFrom(skip)
									}
								}

								// jumpTo end of close, so first open animation starts at beginning as closeTimeline.currentTime is at end
								closeAppointmentOpenAnimations.forEach { ani -> ani.forEach { it.jumpTo(it.totalDuration) } }
								closeTimeline.jumpTo(closeTimeline.totalDuration)

								onMouseClicked = EventHandler {
									if(selectedIndex.value != index) {
										selectedIndex.value = index
										addClass(OverviewStyles.toggledRow_)
									} else {
										selectedIndex.value = -1
										removeClass(OverviewStyles.toggledRow_)
									}
									if(it.clickCount > 1) {
										log(
											"click week: $week   day:${
												week.allDays.values.toTypedArray().getOrNull(hoveredCell.value - 1)
											}", LogType.LOW
										)
										TabManager.openTab( // "${time.dayOfMonth} - ${time.plusDays(6).dayOfMonth} / ${getLangString(time.month.name)}"
											"Week${week.time.dayOfMonth}/${week.time.year}",
											::createWeekTab,
											week,
											week.allDays.values.toTypedArray().getOrNull(hoveredCell.value - 1), {
												log("update from Week triggered")
												updateTable(overviewWeeks)
											}
										)
									}
								}

								selectedIndex.addListener(ChangeListener { _, old, new ->
									if(new != index) {
										removeClass(OverviewStyles.toggledRow_)
										if(old == index && (new != -1)) {
											onMouseExited.handle(null)
										}
									}
								})
							}
						}
						*/
					}
				}
			}
		}
	}
}

const val DETAILSPANMINHEIGHT = 8

fun createCellGraphics(
	data: CellDisplay, source: HBox, openTimeline: Timeline, closeTimeline: Timeline, expand: SimpleDoubleProperty
): Array<Any> {
	val animations: Array<MutableList<Animation>> = arrayOf(mutableListOf(), mutableListOf())
	val graphicContainer = source.vbox {
		addClass(GlobalStyles.tableItem_)
		addClass(OverviewStyles.cell_)
		
		if(data is Day) {
			if(!data.partOfMonth) addClass(OverviewStyles.disabledCell_)
			
			gridpane {
				style {
					prefWidth = Int.MAX_VALUE.px
					padding = box(0.px, 3.px, 2.px, 3.px)
				}
				anchorpane {
					val defaultImg = createFXImage("remind.svg")
					val hoveredImg = createFXImage("remind hovered.svg")
					
					val img = imageview(defaultImg) {
						addClass(OverviewStyles.cellIcon_)
						fitHeight = 21.5
						fitWidth = 21.5
					}
					onMouseClicked = EventHandler {
						it.consume()
						ReminderPopup.open(
							"new reminder".translate(Language.TranslationTypes.ReminderPopup),
							"create".translate(Language.TranslationTypes.ReminderPopup),
							false,
							null,
							data.time.atStartOfDay(),
							false
						)
					}
					onMouseEntered = EventHandler { img.image = hoveredImg }
					onMouseExited = EventHandler { img.image = defaultImg }
				}
				
				label(data.time.dayOfMonth.toString()) {
					gridpaneConstraints {
						columnRowIndex(1, 0)
					}
					addClass(OverviewStyles.cellLabel_)
				}
				anchorpane {
					val defaultImg = if(data.notes.isEmpty()) createFXImage("note.svg")
					else createFXImage("note active.svg")
					val hoveredImg = if(data.notes.isEmpty()) createFXImage("note hovered.svg")
					else createFXImage("note active hovered.svg")
					
					val img = imageview(defaultImg) {
						addClass(OverviewStyles.cellIcon_)
						fitHeight = 21.5
						fitWidth = 21.5
					}
					gridpaneConstraints {
						columnRowIndex(2, 0)
					}
					onMouseClicked = EventHandler {
						it.consume()
						TabManager.openTab("DayNotes${data.time.dayOfMonth}/${data.time.month}/${data.time.year}",
							::createNoteTab,
							data,
							{
//							if(overviewTime.month == data.time.month || overviewTime.month == data.time.plusMonths(1).month || overviewTime.month == data.time.minusMonths(1).month) {
//								log("reloading Month ${data.time.month} from updateCallback", LogType.NORMAL)
//								val weeksData = generateMonth(overviewTime)
//								overviewWeeks.clear()
//								overviewWeeks.addAll(weeksData)
//							}
							})
					}
					onMouseEntered = EventHandler { img.image = hoveredImg }
					onMouseExited = EventHandler { img.image = defaultImg }
				}
			}
			
		} else if(data is Week) {
			addClass(OverviewStyles.disabledCell_)
			gridpane {
				style {
					prefWidth = Int.MAX_VALUE.px
					padding = box(0.px, 3.px, 2.px, 3.px)
				}
				
				anchorpane {
					gridpaneConstraints {
						columnRowIndex(0, 0)
					}
				}
				
				label(data.WeekOfYear.toString()) {
					gridpaneConstraints {
						columnRowIndex(1, 0)
					}
					addClass(OverviewStyles.cellLabel_)
				}
				
				anchorpane {
					val defaultImg = if(data.notes.isEmpty()) createFXImage("note.svg")
					else createFXImage("note active.svg")
					val hoveredImg = if(data.notes.isEmpty()) createFXImage("note hovered.svg")
					else createFXImage("note active hovered.svg")
					
					val img = imageview(defaultImg) {
						addClass(OverviewStyles.cellIcon_)
						fitHeight = 21.5
						fitWidth = 21.5
					}
					gridpaneConstraints {
						columnRowIndex(2, 0)
					}
					onMouseClicked = EventHandler {
						it.consume()
						TabManager.openTab("WeekNotes${data.WeekOfYear}/${data.time.year}", ::createNoteTab, data)
					}
					onMouseEntered = EventHandler { img.image = hoveredImg }
					onMouseExited = EventHandler { img.image = defaultImg }
				}
			}
		}
		
		// appointments / WeekDetails
		val pane = pane {
			style(append = true) {
//				BGColor += Color.RED
				// must be deactivated because style gets reset
				// when any (mouse)events happen
				prefHeight = DETAILSPANMINHEIGHT.px
//				minHeight = DETAILSPANMINHEIGHT.px
			}
		}
		
		if((data is Day && data.appointments.isNotEmpty()) || (data is Week && data.appointments.isNotEmpty())) {
			val expandHeight = when(data) {
				is Day -> generateAppointmentsGraphic(data, pane, animations)
				is Week -> generateWeekGraphic(data, pane, animations)
				else -> 0.0
			}
			
			if(expand.value < expandHeight) expand.value = expandHeight
		}
		
		var open = KeyValue(pane.minHeightProperty(), expand.value)
		var closeFrame = KeyFrame(Duration(0.0), open)
		var openFrame = KeyFrame(Duration(getConfig(Configs.AnimationSpeed)), open)
		
		expand.addListener(ChangeListener { _, _, new: Number ->
			openTimeline.keyFrames.remove(openFrame)
			closeTimeline.keyFrames.remove(closeFrame)
			open = KeyValue(pane.minHeightProperty(), new)
			closeFrame = KeyFrame(Duration(0.0), open)
			openFrame = KeyFrame(Duration(getConfig(Configs.AnimationSpeed)), open)
			openTimeline.keyFrames.add(openFrame)
			closeTimeline.keyFrames.add(closeFrame)
		})
		
		openTimeline.keyFrames.add(KeyFrame(Duration(0.0), KeyValue(pane.minHeightProperty(), DETAILSPANMINHEIGHT)))
		openTimeline.keyFrames.add(openFrame)
		
		closeTimeline.keyFrames.add(closeFrame)
		closeTimeline.keyFrames.add(
			KeyFrame(
				Duration(getConfig(Configs.AnimationSpeed)), KeyValue(pane.minHeightProperty(), DETAILSPANMINHEIGHT)
			)
		)
		
		pane.widthProperty().addListener { _ ->
			if(data is Day) {
				if(data.appointments.isEmpty()) return@addListener
				generateAppointmentsGraphic(data, pane, animations)
			} else if(data is Week) generateWeekGraphic(data, pane, animations)
		}
	}
	
	return arrayOf(graphicContainer, animations[0], animations[1])
}


const val SPACING = 4.0
const val CIRCLE_WIDTH = 8.0

const val SIDE_TOP_MARGIN = 6.0
const val VERTICAL_TOP_MARGIN = 4.0
const val HORIZONTAL_LEFT_MARGIN = 8.0

fun generateWeekGraphic(week: Week, pane: Pane, animations: Array<MutableList<Animation>>): Double {
	pane.clear()
	
	// make width even because ,5 pixel are not supported <-(AI said this)
	val width = (if(pane.width.toInt() % 2 == 0) pane.width.toInt() else pane.width.toInt() + 1).toDouble()
	
	val yCords = mutableListOf<Double>()
	for(index in 0 until week.getAllAppointmentsSorted().size) {
		yCords.add(SIDE_TOP_MARGIN + index * (SPACING * 2 + CIRCLE_WIDTH))
	}
	
	for((index, appointmentEntry) in week.getAllAppointmentsSorted().entries.withIndex()) {
		pane.hbox(alignment = Pos.CENTER_LEFT, spacing = SPACING) {
			circle(radius = CIRCLE_WIDTH / 2) {
				fill = appointmentEntry.key.color.value // TODO bind
			}
			label("${appointmentEntry.value}:${appointmentEntry.key.name}") {
				addClass(OverviewStyles.cellAppointTypeLabel_)
				maxWidth = width - HORIZONTAL_LEFT_MARGIN - CIRCLE_WIDTH
				ellipsisString = ".."
				textOverrun = OverrunStyle.ELLIPSIS
			}
			
			translateX = CIRCLE_WIDTH / 2
			translateY = yCords[index] - CIRCLE_WIDTH
			opacity = 0.0
		}
	}
	
	val openTransitions = mutableListOf<Animation>()
	val closeTransitions = mutableListOf<Animation>()
	
	for(label in pane.getChildList()?.filterIsInstance<HBox>()!!) {
		val openFadeTransition = Timeline(
			KeyFrame(Duration(0.0), KeyValue(label.opacityProperty(), 0.0)), KeyFrame(
				Duration(getConfig<Double>(Configs.AnimationSpeed) / 3 * 2), KeyValue(label.opacityProperty(), 0.0)
			), KeyFrame(Duration(getConfig(Configs.AnimationSpeed)), KeyValue(label.opacityProperty(), 1.0))
		)
		openTransitions.add(openFadeTransition)
		
		val closeFadeTransition = Timeline(
			KeyFrame(Duration(0.0), KeyValue(label.opacityProperty(), 1.0)),
			KeyFrame(Duration(getConfig<Double>(Configs.AnimationSpeed) / 3), KeyValue(label.opacityProperty(), 0.0)),
			KeyFrame(Duration(getConfig(Configs.AnimationSpeed)), KeyValue(label.opacityProperty(), 0.0))
		)
		closeTransitions.add(closeFadeTransition)
	}
	
	animations[0].clear()
	animations[0].addAll(openTransitions)
	animations[1].clear()
	animations[1].addAll(closeTransitions)
	
	return (yCords.getOrNull(yCords.lastIndex)?.plus(SIDE_TOP_MARGIN) ?: 0.0)
}

fun generateAppointmentsGraphic(day: Day, pane: Pane, animations: Array<MutableList<Animation>>): Double {
	pane.clear()
	
	var appointments = day.getAppointmentsLimited()
	val limited = appointments.size != day.appointments.size
	if(limited) appointments = appointments.dropLast(1)
	
	// make width even because ,5 pixel are not supported <-(AI said this)
	val width = (if(pane.width.toInt() % 2 == 0) pane.width.toInt() else pane.width.toInt() + 1).toDouble()
	
	val topSpacing: Double = maxOf(2.0, minOf(SPACING, (((width - SPACING) / appointments.size) / 3)))
	val topCircleWidth: Double = topSpacing * 2
	
	val xCords = mutableListOf<Double>()
	
	if(appointments.size % 2 == 0) {
		for(index in 0 until appointments.size / 2) {
			xCords.add((width / 2) + ((topSpacing / 2) + (index * (topCircleWidth + topSpacing)) + topCircleWidth / 2))
			xCords.add((width / 2) - ((topSpacing / 2) + (index * (topCircleWidth + topSpacing)) + topCircleWidth / 2))
		}
	} else {
		xCords.add(width / 2)
		for(index in 0 until (appointments.size - 1) / 2) {
			xCords.add((width / 2) + ((topCircleWidth / 2) + topSpacing + (index * (topCircleWidth + topSpacing)) + topCircleWidth / 2))
			xCords.add((width / 2) - ((topCircleWidth / 2) + topSpacing + (index * (topCircleWidth + topSpacing)) + topCircleWidth / 2))
		}
	}
	
	// better animation because vertical are sorted from top to bottom
	xCords.sortDescending()
	
	for((index, appointment) in appointments.withIndex()) {
		pane.circle(radius = topCircleWidth / 2) {
			fill = appointment.type.value.color.value // bind
			centerY = VERTICAL_TOP_MARGIN
			centerX = xCords[index]
		}
	}
	
	val yCords = mutableListOf<Double>()
	for(index in appointments.indices) {
		yCords.add(SIDE_TOP_MARGIN + index * (SPACING + CIRCLE_WIDTH))
	}
	if(limited) yCords.add(SIDE_TOP_MARGIN + yCords.size * (SPACING + CIRCLE_WIDTH))
	
	for((index, appointment) in appointments.withIndex()) {
		pane.label(appointment.title) {
			addClass(OverviewStyles.cellAppointLabel_)
			translateX = HORIZONTAL_LEFT_MARGIN + CIRCLE_WIDTH
			translateY = yCords[index] - CIRCLE_WIDTH / 1.1
			opacity = 0.0
			
			maxWidth = width - HORIZONTAL_LEFT_MARGIN - CIRCLE_WIDTH
			ellipsisString = ".."
			textOverrun = OverrunStyle.ELLIPSIS
		}
	}
	
	if(limited) {
		pane.label("· · · · · · · · · · · · · · · · · · · · · · · ·") {
			addClass(OverviewStyles.cellAppointTypeLabel_)
			style {
				fontWeight = FontWeight.BOLD
			}
			translateX = HORIZONTAL_LEFT_MARGIN
			translateY = yCords[yCords.size - 1] - CIRCLE_WIDTH //- SPACING
			opacity = 0.0
			
			maxWidth = width - HORIZONTAL_LEFT_MARGIN
			ellipsisString = ""
			textOverrun = OverrunStyle.ELLIPSIS
		}
	}
	
	val openTransitions = mutableListOf<Animation>()
	val closeTransitions = mutableListOf<Animation>()
	
	for(index in appointments.indices) {
		val circle = pane.getChildList()?.filterIsInstance<Circle>()?.get(index)
		val label = pane.getChildList()?.filterIsInstance<Label>()?.get(index)
		
		val openPath = Path()
		openPath.elements.add(MoveTo(xCords[index], SIDE_TOP_MARGIN))
		openPath.elements.add(
			CubicCurveTo(
				xCords[index],
				SIDE_TOP_MARGIN,
				HORIZONTAL_LEFT_MARGIN * 1.8,
				SIDE_TOP_MARGIN * 1.8,
				HORIZONTAL_LEFT_MARGIN,
				yCords[index]
			),
		)
		openTransitions.add(PathTransition(Duration(getConfig(Configs.AnimationSpeed)), openPath, circle))
		
		val openFadeTransition = Timeline(
			KeyFrame(Duration(0.0), KeyValue(label?.opacityProperty(), 0.0)), KeyFrame(
				Duration(getConfig<Double>(Configs.AnimationSpeed) / 3 * 2), KeyValue(label?.opacityProperty(), 0.0)
			), KeyFrame(Duration(getConfig(Configs.AnimationSpeed)), KeyValue(label?.opacityProperty(), 1.0))
		)
		openTransitions.add(openFadeTransition)
		
		val closePath = Path()
		closePath.elements.add(MoveTo(HORIZONTAL_LEFT_MARGIN, yCords[index]))
		closePath.elements.add(
			CubicCurveTo(
				HORIZONTAL_LEFT_MARGIN, yCords[index], xCords[index], yCords[index], xCords[index], VERTICAL_TOP_MARGIN
			)
		)
		closeTransitions.add(PathTransition(Duration(getConfig(Configs.AnimationSpeed)), closePath, circle))
		
		val closeFadeTransition = Timeline(
			KeyFrame(Duration(0.0), KeyValue(label?.opacityProperty(), 1.0)),
			KeyFrame(Duration(getConfig<Double>(Configs.AnimationSpeed) / 3), KeyValue(label?.opacityProperty(), 0.0)),
			KeyFrame(Duration(getConfig(Configs.AnimationSpeed)), KeyValue(label?.opacityProperty(), 0.0))
		)
		closeTransitions.add(closeFadeTransition)
	}
	
	if(limited) {
		val label = pane.getChildList()?.filterIsInstance<Label>()?.last()
		val openFadeTransition = Timeline(
			KeyFrame(Duration(0.0), KeyValue(label?.opacityProperty(), 0.0)), KeyFrame(
				Duration(getConfig<Double>(Configs.AnimationSpeed) / 3 * 2), KeyValue(label?.opacityProperty(), 0.0)
			), KeyFrame(Duration(getConfig(Configs.AnimationSpeed)), KeyValue(label?.opacityProperty(), 1.0))
		)
		openTransitions.add(openFadeTransition)
		
		val closeFadeTransition = Timeline(
			KeyFrame(Duration(0.0), KeyValue(label?.opacityProperty(), 1.0)),
			KeyFrame(Duration(getConfig<Double>(Configs.AnimationSpeed) / 3), KeyValue(label?.opacityProperty(), 0.0)),
			KeyFrame(Duration(getConfig(Configs.AnimationSpeed)), KeyValue(label?.opacityProperty(), 0.0))
		)
		closeTransitions.add(closeFadeTransition)
	}
	
	animations[0].clear()
	animations[0].addAll(openTransitions)
	animations[1].clear()
	animations[1].addAll(closeTransitions)
	
	return (yCords.getOrNull(yCords.lastIndex)?.plus(SIDE_TOP_MARGIN) ?: 0.0)
}
