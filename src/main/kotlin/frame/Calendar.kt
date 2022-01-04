package frame

import calendar.CellDisplay
import calendar.Day
import calendar.Week
import calendar.calendarDisplay
import calendar.changeMonth
import calendar.currentMonth
import calendar.currentMonthName
import calendar.generateMonth
import calendar.now
import calendar.prepareMonthNotes
import calendar.preparedDayNotes
import calendar.preparedWeekNotes
import javafx.animation.*
import javafx.beans.property.*
import javafx.collections.*
import javafx.event.*
import javafx.geometry.*
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.paint.*
import javafx.scene.shape.*
import javafx.scene.text.*
import javafx.util.*
import listen
import logic.Configs
import logic.LogType
import logic.getConfig
import logic.getLangString
import logic.log
import tornadofx.*


fun createcalendartab(pane: TabPane): Tab {
	log("creating calendar tab", LogType.IMPORTANT)
	return pane.tab(getLangString("calender")) {
		isClosable = false
		
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
				
				log("creating top bar", LogType.LOW)
				// Top bar
				hbox(spacing = 40.0, alignment = Pos.CENTER) {
					addClass(Styles.Tabs.topbar)
					style {
						padding = box(0.px, 15.px, 0.px, 15.px)
					}
					button("<") {
						addClass(Styles.Tabs.titleButtons)
						action {
							changeMonth(false)
						}
					}
					label(currentMonthName) {
						addClass(Styles.Tabs.title)
						minWidth = 200.0
						alignment = Pos.CENTER
					}
					button(">") {
						addClass(Styles.Tabs.titleButtons)
						action {
							changeMonth(true)
						}
					}
				}
				
				separate()
				
				log("creating table view", LogType.LOW)
				// Table view
				vbox(spacing = 1.0, alignment = Pos.TOP_CENTER) {
					addClass(Styles.CalendarTableView.table)
					
					lateinit var scrollbarWidth: DoubleProperty
					
					// Top bar
					hbox(spacing = 5.0, alignment = Pos.CENTER) {
						padding = Insets(3.0)
						style {
							backgroundColor += Color.WHITE
						}
						scrollbarWidth = paddingRightProperty
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
					
					var table: ScrollPane? = null
					
					fun updateTable(list: ObservableList<out Week>) {
						children.remove(table)
						log("updated table view", LogType.LOW)
						val selectedIndex = SimpleIntegerProperty(-1)
						
						table = scrollpane(fitToWidth = true) {
							
							// update top bar fake scrollbar padding  (wait for width update,so that scrollbars were created already; and then update if scrollbar width changes[appears/disappears])
							widthProperty().listen(once = true) {
								lookupAll(".scroll-bar").filterIsInstance<ScrollBar>().filter { it.orientation == Orientation.VERTICAL }[0].let { bar ->
									bar.visibleProperty()
										.listen {
											if(it)
												scrollbarWidth.value = bar.width + 2 // 2 padding right of inner vbox
											else
												scrollbarWidth.value = 2.0 // 2 padding right of inner vbox
										}
								}
							}
							
							style {
								borderWidth += box(1.px)
								borderColor += box(Color.WHITE)
							}
							vbox(spacing = 5.0, alignment = Pos.CENTER) {
								style(append = true) {
									backgroundColor += Color.WHITE
									paddingRight = 2.0
								}
								for((index, week) in list.withIndex()) {
									hbox(spacing = 5.0, alignment = Pos.CENTER) {
										style(append = true) {
											backgroundColor += Color.WHITE
										}
										
										val openTimeline = Timeline()
										val closeTimeline = Timeline()
										
										val cells = mutableListOf<VBox>()
										
										val expand = SimpleDoubleProperty(detailspaneminHeight.toDouble())
										
										val openAppointmentOpenAnimations: MutableList<MutableList<Animation>> = mutableListOf()
										val closeAppointmentOpenAnimations: MutableList<MutableList<Animation>> = mutableListOf()
										
										val temp = createCellGraphics(week, this@hbox, openTimeline, closeTimeline, expand)
										cells.add(temp[0] as VBox)
										@Suppress("UNCHECKED_CAST")
										openAppointmentOpenAnimations.add(temp[1] as MutableList<Animation>)
										@Suppress("UNCHECKED_CAST")
										closeAppointmentOpenAnimations.add(temp[2] as MutableList<Animation>)
										
										week.allDays.values.forEach {
											val tmp = createCellGraphics(it, this@hbox, openTimeline, closeTimeline, expand)
											cells.add(tmp[0] as VBox)
											
											if(it.time.dayOfYear == now.dayOfYear && it.time.year == now.year)
												(tmp[0] as VBox).addClass(Styles.CalendarTableView.markedTableCell)
											
											@Suppress("UNCHECKED_CAST")
											openAppointmentOpenAnimations.add(tmp[1] as MutableList<Animation>)
											@Suppress("UNCHECKED_CAST")
											closeAppointmentOpenAnimations.add(tmp[2] as MutableList<Animation>)
										}
										
										val hoveredCell = SimpleIntegerProperty(-1)
										
										for((cellIndex, cell) in cells.withIndex()) {
											cell.onMouseEntered = EventHandler {
												cell.addClass(Styles.CalendarTableView.hoveredTableCell)
												hoveredCell.value = cellIndex
											}
											cell.onMouseExited = EventHandler {
												cell.removeClass(Styles.CalendarTableView.hoveredTableCell)
												hoveredCell.value = -1
											}
											cell.widthProperty().addListener { _, _, _ -> selectedIndex.value = -2 /*-1 doesn't close -2 forces close of row*/ }
										}
										
										var openPreparation = false
										
										onMouseEntered = EventHandler {
											if(selectedIndex.value != index) {
												openPreparation = true
												Thread {
													Thread.sleep(getConfig<Double>(Configs.AnimationDelay).toLong())
													if(openPreparation) {
														openPreparation = false
														openAppointmentOpenAnimations.forEach { ani -> ani.forEach { it.play() } }
														openTimeline.play()
													}
												}.start()
											}
										}
										
										onMouseExited = EventHandler {
											if(selectedIndex.value != index) {
												if(openPreparation)
													openPreparation = false
												else {
													openAppointmentOpenAnimations.forEach { ain -> ain.forEach { it.stop() } }
													openTimeline.stop()
													closeAppointmentOpenAnimations.forEach { ani -> ani.forEach { it.play() } }
													closeTimeline.play()
												}
											}
										}
										
										onMouseClicked = EventHandler {
											if(selectedIndex.value != index) {
												selectedIndex.value = index
												addClass(Styles.CalendarTableView.selectedColumn)
											} else {
												selectedIndex.value = -1
												removeClass(Styles.CalendarTableView.selectedColumn)
											}
											if(it.clickCount > 1) {
												log(
													"click week: $week   day:${
														week.allDays.values.toTypedArray().getOrNull(hoveredCell.value - 1)
													}", LogType.LOW
												)
												Tabmanager.openTab(
													"Week${week.date}/${week.time.year}",
													::createWeekTab,
													week,
													week.allDays.values.toTypedArray().getOrNull(hoveredCell.value - 1), {
														log("update from Week triggered")  // TODO Update
													}
												)
											}
										}
										
										selectedIndex.addListener(ChangeListener { _, old, new ->
											if(new != index) {
												removeClass(Styles.CalendarTableView.selectedColumn)
												if(old == index && (new != -1 || new == -2)) {
													onMouseExited.handle(null)
												}
											}
										})
									}
								}
							}
						}
					}
					
					currentMonth.addListener(ListChangeListener {
						updateTable(it.list)
					})
					
					updateTable(currentMonth)
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

const val detailspaneminHeight = 8

fun createCellGraphics(
	data: CellDisplay,
	source: HBox,
	openTimeline: Timeline,
	closeTimeline: Timeline,
	expand: SimpleDoubleProperty
): Array<Any> {
	val animations: Array<MutableList<Animation>> = arrayOf(mutableListOf(), mutableListOf())
	val graphicContainer = source.vbox {
		addClass(Styles.CalendarTableView.tableItem)
		addClass(Styles.CalendarTableView.tableCell)
		
		if(data is Day) {
			if(!data.partOfMonth)
				addClass(Styles.CalendarTableView.disabledTableCell)
			
			gridpane {
				style {
					prefWidth = Int.MAX_VALUE.px
					padding = box(0.px, 3.px, 2.px, 3.px)
				}
				anchorpane {
					val defaultImg = if(true)
						createFXImage("remind.svg")
					else
						createFXImage("remind active.svg")
					val hoveredImg = if(true)
						createFXImage("remind hovered.svg")
					else
						createFXImage("remind active hovered.svg")
					
					val img = imageview(defaultImg) {
						addClass(Styles.CalendarTableView.cellLabelIcon)
						fitHeight = 21.5
						fitWidth = 21.5
					}
					onMouseClicked = EventHandler { it.consume() }
					onMouseEntered = EventHandler { img.image = hoveredImg }
					onMouseExited = EventHandler { img.image = defaultImg }
				}
				
				label(data.time.dayOfMonth.toString()) {
					gridpaneConstraints {
						columnRowIndex(1, 0)
					}
					addClass(Styles.CalendarTableView.cellLabel)
				}
				anchorpane {
					val defaultImg = if(data.notes.isEmpty())
						createFXImage("note.svg")
					else
						createFXImage("note active.svg")
					val hoveredImg = if(data.notes.isEmpty())
						createFXImage("note hovered.svg")
					else
						createFXImage("note active hovered.svg")
					
					val img = imageview(defaultImg) {
						addClass(Styles.CalendarTableView.cellLabelIcon)
						fitHeight = 21.5
						fitWidth = 21.5
					}
					gridpaneConstraints {
						columnRowIndex(2, 0)
					}
					onMouseClicked = EventHandler {
						it.consume()
						Tabmanager.openTab("DayNotes${data.time.dayOfMonth}/${data.time.month}/${data.time.year}", ::createNoteTab, data, {
							if(calendarDisplay.month == data.time.month || calendarDisplay.month == data.time.plusMonths(1).month || calendarDisplay.month == data.time.minusMonths(1).month) {
								log("reloading Month ${data.time.month} from updateCallback", LogType.NORMAL)
								
								preparedWeekNotes[data.time.month]?.clear()
								preparedDayNotes[data.time.month]?.clear()
								prepareMonthNotes(data.time.month)
								val weeksData = generateMonth(calendarDisplay)
								currentMonth.clear()
								currentMonth.addAll(weeksData)
							}
						})
					}
					onMouseEntered = EventHandler { img.image = hoveredImg }
					onMouseExited = EventHandler { img.image = defaultImg }
				}
			}
			
		} else if(data is Week) {
			addClass(Styles.CalendarTableView.disabledTableCell)
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
					addClass(Styles.CalendarTableView.cellLabel)
				}
				
				anchorpane {
					val defaultImg = if(data.notes.isEmpty())
						createFXImage("note.svg")
					else
						createFXImage("note active.svg")
					val hoveredImg = if(data.notes.isEmpty())
						createFXImage("note hovered.svg")
					else
						createFXImage("note active hovered.svg")
					
					val img = imageview(defaultImg) {
						addClass(Styles.CalendarTableView.cellLabelIcon)
						fitHeight = 21.5
						fitWidth = 21.5
					}
					gridpaneConstraints {
						columnRowIndex(2, 0)
					}
					onMouseClicked = EventHandler {
						it.consume()
						Tabmanager.openTab(
							"WeekNotes${data.WeekOfYear}/${data.time.year}", ::createNoteTab, data, {
								if(calendarDisplay.month == data.time.month || calendarDisplay.month == data.time.plusMonths(1).month || calendarDisplay.month == data.time.minusMonths(1).month) {
									log("reloading Month ${data.time.month} from updateCallback", LogType.NORMAL)
									
									preparedWeekNotes[data.time.month]?.clear()
									preparedDayNotes[data.time.month]?.clear()
									prepareMonthNotes(data.time.month)
									val weeksData = generateMonth(calendarDisplay)
									currentMonth.clear()
									currentMonth.addAll(weeksData)
								}
							}
						)
					}
					onMouseEntered = EventHandler { img.image = hoveredImg }
					onMouseExited = EventHandler { img.image = defaultImg }
				}
			}
		}
		
		// appointments / WeekDetails
		val pane = pane {
			style(append = true) {
//				backgroundColor += Color.RED
				// must be deactivated because style gets reset
				// when any (mouse)events happen
				prefHeight = detailspaneminHeight.px
//				minHeight = detailspaneminHeight.px
			}
		}
		
		if((data is Day && data.appointments.isNotEmpty()) || (data is Week && data.appointments.isNotEmpty())) {
			val expandHeight = when(data) {
				is Day -> generateAppointmentsGraphic(data, pane, animations)
				is Week -> generateWeekGraphic(data, pane, animations)
				else -> 0.0
			}
			
			if(expand.value < expandHeight)
				expand.value = expandHeight
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
		
		openTimeline.keyFrames.add(KeyFrame(Duration(0.0), KeyValue(pane.minHeightProperty(), detailspaneminHeight)))
		openTimeline.keyFrames.add(openFrame)
		
		closeTimeline.keyFrames.add(closeFrame)
		closeTimeline.keyFrames.add(
			KeyFrame(
				Duration(getConfig(Configs.AnimationSpeed)),
				KeyValue(pane.minHeightProperty(), detailspaneminHeight)
			)
		)
		
		pane.widthProperty().addListener { _ ->
			if(data is Day) {
				if(data.appointments.isEmpty())
					return@addListener
				generateAppointmentsGraphic(data, pane, animations)
			} else if(data is Week)
				generateWeekGraphic(data, pane, animations)
		}
	}
	
	return arrayOf(graphicContainer, animations[0], animations[1])
}


const val spacing = 4.0
const val circleWidth = 8.0

const val sideTopMargin = 6.0
const val verticalTopMargin = 4.0
const val horizontalLeftMargin = 8.0

fun generateWeekGraphic(week: Week, pane: Pane, animations: Array<MutableList<Animation>>): Double {
	pane.clear()
	
	// make width even because ,5 pixel are not supported <-(AI said this)
	val width = (if(pane.width.toInt() % 2 == 0) pane.width.toInt() else pane.width.toInt() + 1).toDouble()
	
	val yCords = mutableListOf<Double>()
	for(index in 0 until week.getallAppointmentsSorted().size) {
		yCords.add(sideTopMargin + index * (spacing * 2 + circleWidth))
	}
	
	for((index, appointmentEntry) in week.getallAppointmentsSorted().entries.withIndex()) {
		pane.hbox(alignment = Pos.CENTER_LEFT, spacing = spacing) {
			circle(radius = circleWidth / 2) {
				fill = appointmentEntry.key.color
			}
			label("${appointmentEntry.value.size}:${appointmentEntry.key.name}") {
				addClass(Styles.CalendarTableView.cellAppointTypeLabel)
				maxWidth = width - horizontalLeftMargin - circleWidth
				ellipsisString = ".."
				textOverrun = OverrunStyle.ELLIPSIS
			}
			
			translateX = circleWidth / 2
			translateY = yCords[index] - circleWidth
			opacity = 0.0
		}
	}
	
	val openTransitions = mutableListOf<Animation>()
	val closeTransitions = mutableListOf<Animation>()
	
	for(label in pane.getChildList()?.filterIsInstance<HBox>()!!) {
		val openFadeTransition = Timeline(
			KeyFrame(Duration(0.0), KeyValue(label.opacityProperty(), 0.0)),
			KeyFrame(Duration(getConfig<Double>(Configs.AnimationSpeed) / 3 * 2), KeyValue(label.opacityProperty(), 0.0)),
			KeyFrame(Duration(getConfig(Configs.AnimationSpeed)), KeyValue(label.opacityProperty(), 1.0))
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
	
	return (yCords.getOrNull(yCords.lastIndex)?.plus(sideTopMargin) ?: 0.0)
	
}

fun generateAppointmentsGraphic(day: Day, pane: Pane, animations: Array<MutableList<Animation>>): Double {
	pane.clear()
	
	var appointments = day.getAppointmentsLimited()
	val limited = appointments.size != day.appointments.size
	if(limited)
		appointments = appointments.dropLast(1)
	
	// make width even because ,5 pixel are not supported <-(AI said this)
	val width = (if(pane.width.toInt() % 2 == 0) pane.width.toInt() else pane.width.toInt() + 1).toDouble()
	
	val topSpacing: Double = maxOf(2.0, minOf(spacing, (((width - spacing) / appointments.size) / 3)))
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
			fill = appointment.type.color
			centerY = verticalTopMargin
			centerX = xCords[index]
		}
	}
	
	val yCords = mutableListOf<Double>()
	for(index in appointments.indices) {
		yCords.add(sideTopMargin + index * (spacing + circleWidth))
	}
	if(limited)
		yCords.add(sideTopMargin + yCords.size * (spacing + circleWidth))
	
	for((index, appointment) in appointments.withIndex()) {
		pane.label(appointment.title) {
			addClass(Styles.CalendarTableView.cellAppointLabel)
			translateX = horizontalLeftMargin + circleWidth
			translateY = yCords[index] - circleWidth / 1.1
			opacity = 0.0
			
			maxWidth = width - horizontalLeftMargin - circleWidth
			ellipsisString = ".."
			textOverrun = OverrunStyle.ELLIPSIS
		}
	}
	
	if(limited) {
		pane.label("· · · · · · · · · · · · · · · · · · · · · · · ·") {
			addClass(Styles.CalendarTableView.cellAppointTypeLabel)
			style {
				fontWeight = FontWeight.BOLD
			}
			translateX = horizontalLeftMargin
			translateY = yCords[yCords.size - 1] - circleWidth //- spacing
			opacity = 0.0
			
			maxWidth = width - horizontalLeftMargin
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
		openPath.elements.add(MoveTo(xCords[index], sideTopMargin))
		openPath.elements.add(
			CubicCurveTo(
				xCords[index], sideTopMargin, horizontalLeftMargin * 1.8, sideTopMargin * 1.8, horizontalLeftMargin, yCords[index]
			),
		)
		openTransitions.add(PathTransition(Duration(getConfig(Configs.AnimationSpeed)), openPath, circle))
		
		val openFadeTransition = Timeline(
			KeyFrame(Duration(0.0), KeyValue(label?.opacityProperty(), 0.0)),
			KeyFrame(Duration(getConfig<Double>(Configs.AnimationSpeed) / 3 * 2), KeyValue(label?.opacityProperty(), 0.0)),
			KeyFrame(Duration(getConfig(Configs.AnimationSpeed)), KeyValue(label?.opacityProperty(), 1.0))
		)
		openTransitions.add(openFadeTransition)
		
		val closePath = Path()
		closePath.elements.add(MoveTo(horizontalLeftMargin, yCords[index]))
		closePath.elements.add(
			CubicCurveTo(
				horizontalLeftMargin, yCords[index], xCords[index], yCords[index], xCords[index], verticalTopMargin
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
			KeyFrame(Duration(0.0), KeyValue(label?.opacityProperty(), 0.0)),
			KeyFrame(Duration(getConfig<Double>(Configs.AnimationSpeed) / 3 * 2), KeyValue(label?.opacityProperty(), 0.0)),
			KeyFrame(Duration(getConfig(Configs.AnimationSpeed)), KeyValue(label?.opacityProperty(), 1.0))
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
	
	return (yCords.getOrNull(yCords.lastIndex)?.plus(sideTopMargin) ?: 0.0)
}
