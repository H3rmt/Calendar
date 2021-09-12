package frame

import calendar.Celldisplay
import calendar.Day
import calendar.Week
import calendar.changeMonth
import calendar.currentmonth
import calendar.currentmonthName
import calendar.now
import javafx.animation.*
import javafx.beans.property.*
import javafx.collections.*
import javafx.event.*
import javafx.geometry.*
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.shape.*
import javafx.scene.text.*
import javafx.util.*
import logic.Configs
import logic.LogType
import logic.getConfig
import logic.log
import tornadofx.*


fun createcalendartab(pane: TabPane): Tab {
	log("creating calendar tab", LogType.IMPORTANT)
	return pane.tab("Calender") {
		isClosable = false
		
		stackpane {
			style(append = true) {
				padding = box(6.px)
			}
			
			// maintab
			vbox {
				addClass(Styles.Tabs.maintab)
				
				log("creating top bar", LogType.LOW)
				// Top bar
				hbox(spacing = 40.0, alignment = Pos.CENTER) {
					addClass(Styles.Tabs.topbar)
					style {
					}
					button("<") {
						addClass(Styles.Tabs.titlebuttons)
						action {
							changeMonth(false)
						}
					}
					label(currentmonthName) {
						addClass(Styles.Tabs.title)
						minWidth = 200.0
						alignment = Pos.CENTER
					}
					button(">") {
						addClass(Styles.Tabs.titlebuttons)
						action {
							changeMonth(true)
						}
					}
				}
				
				// seperator
				label {
					addClass(Styles.Tabs.seperator)
					useMaxWidth = true
				}
				
				log("creating table view", LogType.LOW)
				// Table view
				vbox(spacing = 1.0, alignment = Pos.TOP_CENTER) {
					addClass(Styles.CalendarTableView.table)
					
					// Top bar
					hbox(spacing = 5.0, alignment = Pos.CENTER) {
						label("") {
							addClass(Styles.CalendarTableView.tableitem)
						}
						label("Monday") {
							addClass(Styles.CalendarTableView.tableitem)
							addClass(Styles.CalendarTableView.tableheader)
							addClass(Styles.CalendarTableView.cellheaderlabel)
						}
						label("Tuesday") {
							addClass(Styles.CalendarTableView.tableitem)
							addClass(Styles.CalendarTableView.tableheader)
							addClass(Styles.CalendarTableView.cellheaderlabel)
						}
						label("Wednesday") {
							addClass(Styles.CalendarTableView.tableitem)
							addClass(Styles.CalendarTableView.tableheader)
							addClass(Styles.CalendarTableView.cellheaderlabel)
						}
						label("Thursday") {
							addClass(Styles.CalendarTableView.tableitem)
							addClass(Styles.CalendarTableView.tableheader)
							addClass(Styles.CalendarTableView.cellheaderlabel)
						}
						label("Friday") {
							addClass(Styles.CalendarTableView.tableitem)
							addClass(Styles.CalendarTableView.tableheader)
							addClass(Styles.CalendarTableView.cellheaderlabel)
						}
						label("Saturday") {
							addClass(Styles.CalendarTableView.tableitem)
							addClass(Styles.CalendarTableView.tableheader)
							addClass(Styles.CalendarTableView.cellheaderlabel)
						}
						label("Sunday") {
							addClass(Styles.CalendarTableView.tableitem)
							addClass(Styles.CalendarTableView.tableheader)
							addClass(Styles.CalendarTableView.cellheaderlabel)
						}
					}
					
					// remove existing columns without removing header
					val columnlist: MutableList<HBox> = mutableListOf()
					
					fun updateTable(list: ObservableList<out Week>) {
						children.removeAll(columnlist)
						columnlist.clear()
						
						log("updated table view", LogType.LOW)
						
						val selectedindex = SimpleIntegerProperty(-1)
						
						for((index, week) in list.withIndex()) {
							hbox(spacing = 5.0, alignment = Pos.CENTER) {
								columnlist.add(this@hbox)
								
								padding = Insets(3.0)
								
								val opentimeline = Timeline()
								val closetimeline = Timeline()
								
								val cells = mutableListOf<VBox>()
								
								val expand = SimpleDoubleProperty(detailspaneminHeight.toDouble())
								
								val openappointmentopenanimations: MutableList<MutableList<Animation>> = mutableListOf()
								val closeappointmentopenanimations: MutableList<MutableList<Animation>> = mutableListOf()
								
								
								val temp = createCellGraphics(week, this@hbox, opentimeline, closetimeline, expand)
								cells.add(temp[0] as VBox)
								@Suppress("UNCHECKED_CAST")
								openappointmentopenanimations.add(temp[1] as MutableList<Animation>)
								@Suppress("UNCHECKED_CAST")
								closeappointmentopenanimations.add(temp[2] as MutableList<Animation>)
								
								week.alldays.values.forEach {
									val tmp = createCellGraphics(it, this@hbox, opentimeline, closetimeline, expand)
									cells.add(tmp[0] as VBox)
									
									if(it.time.dayOfYear == now.dayOfYear && it.time.year == now.year)
										(tmp[0] as VBox).addClass(Styles.CalendarTableView.markedtablecell)
									
									@Suppress("UNCHECKED_CAST")
									openappointmentopenanimations.add(tmp[1] as MutableList<Animation>)
									@Suppress("UNCHECKED_CAST")
									closeappointmentopenanimations.add(tmp[2] as MutableList<Animation>)
								}
								
								val hoveredcell = SimpleIntegerProperty(-1)
								
								for((cellindex, cell) in cells.withIndex()) {
									cell.onMouseEntered = EventHandler {
										cell.addClass(Styles.CalendarTableView.hoveredtablecell)
										hoveredcell.value = cellindex
									}
									cell.onMouseExited = EventHandler {
										cell.removeClass(Styles.CalendarTableView.hoveredtablecell)
										hoveredcell.value = -1
									}
									cell.widthProperty().addListener { _, _, _ -> selectedindex.value = -2 /*-1 doesnt close -2 forces close*/ }
								}
								
								var openprep = false
								
								onMouseEntered = EventHandler {
									if(selectedindex.value != index) {
										openprep = true
										Thread {
											Thread.sleep(getConfig<Double>(Configs.Animationdelay).toLong())
											if(openprep) {
												openprep = false
												openappointmentopenanimations.forEach { ani -> ani.forEach { it.play() } }
												opentimeline.play()
											}
										}.start()
									}
								}
								
								onMouseExited = EventHandler {
									if(selectedindex.value != index) {
										if(openprep)
											openprep = false
										else {
											openappointmentopenanimations.forEach { ain -> ain.forEach { it.stop() } }
											opentimeline.stop()
											closeappointmentopenanimations.forEach { ani -> ani.forEach { it.play() } }
											closetimeline.play()
										}
									}
								}
								
								onMouseClicked = EventHandler {
									if(selectedindex.value != index) {
										selectedindex.value = index
										addClass(Styles.CalendarTableView.selectedcolumn)
									} else {
										selectedindex.value = -1
										removeClass(Styles.CalendarTableView.selectedcolumn)
									}
									if(it.clickCount > 1) {
										log(
											"click week: $week   day:${
												week.alldays.values.toTypedArray().getOrNull(hoveredcell.value - 1)
											}", LogType.LOW
										)
										Tabmanager.openTab(
											"Week${week.toDate()}/${week.time.year}",
											::createweektab,
											week,
											week.alldays.values.toTypedArray().getOrNull(hoveredcell.value - 1)
										)
									}
								}
								
								selectedindex.addListener(ChangeListener { _, old, new ->
									if(new != index) {
										removeClass(Styles.CalendarTableView.selectedcolumn)
										if(old == index && (new != -1 || new == -2)) {
											onMouseExited.handle(null)
										}
									}
								})
							}
						}
					}
					
					currentmonth.addListener(ListChangeListener {
						updateTable(it.list)
					})
					
					updateTable(currentmonth)
				}
			}
			
			// used to shadow the overflow from tab
			pane {
				isMouseTransparent = true
				addClass(Styles.Tabs.shadowborder)
			}
		}
	}
}

const val detailspaneminHeight = 8

fun createCellGraphics(
	data: Celldisplay,
	source: HBox,
	opentimeline: Timeline,
	closetimeline: Timeline,
	expand: SimpleDoubleProperty
): Array<Any?> {
	val animations: Array<MutableList<Animation>> = arrayOf(mutableListOf(), mutableListOf())
	val graphicContainer = source.vbox {
		addClass(Styles.CalendarTableView.tableitem)
		addClass(Styles.CalendarTableView.tablecell)
		
		if(data is Day) {
			if(!data.partofmonth)
				addClass(Styles.CalendarTableView.disabledtablecell)
			
			gridpane {
				style {
					prefWidth = Int.MAX_VALUE.px
					padding = box(0.px, 1.px, 2.px, 1.px)
				}
				anchorpane {
					imageview(FXImage("img/remind.svg")) {
						addClass(Styles.CalendarTableView.celllabelicon)
						fitHeight = 21.0
						fitWidth = 20.0
					}
					onMouseClicked = EventHandler { it.consume() }
					//onMouseEntered = EventHandler { img.image = Image(FileInputStream("img/remind marked.png")) }
					//onMouseExited = EventHandler { img.image = Image(FileInputStream("img/remind.png")) }
				}
				
				label(data.time.dayOfMonth.toString()) {
					gridpaneConstraints {
						columnRowIndex(1, 0)
					}
					addClass(Styles.CalendarTableView.celllabel)
				}
				anchorpane {
					imageview(FXImage("img/note.svg")) {
						addClass(Styles.CalendarTableView.celllabelicon)
						fitHeight = 20.5
						fitWidth = 20.0
					}
					gridpaneConstraints {
						columnRowIndex(2, 0)
					}
					//onMouseEntered = EventHandler { img.image = Image(FileInputStream("img/note marked.png")) }
					//onMouseExited = EventHandler { img.image = Image(FileInputStream("img/note.png")) }
					onMouseClicked = EventHandler {
						it.consume()
						Tabmanager.openTab("DayNotes${data.time.dayOfMonth}/${data.time.month}/${data.time.year}", ::createnotetab, data)
					}
					
				}
			}
			
		} else if(data is Week) {
			addClass(Styles.CalendarTableView.disabledtablecell)
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
				
				label(data.WeekofYear.toString()) {
					gridpaneConstraints {
						columnRowIndex(1, 0)
					}
					addClass(Styles.CalendarTableView.celllabel)
				}
				
				anchorpane {
					imageview(FXImage("img/note.svg")) {
						addClass(Styles.CalendarTableView.celllabelicon)
						fitHeight = 20.5
						fitWidth = 20.0
					}
					gridpaneConstraints {
						columnRowIndex(2, 0)
					}
					//onMouseEntered = EventHandler { img.image = Image(FileInputStream("img/note marked.png")) }
					//onMouseExited = EventHandler { img.image = Image(FileInputStream("img/note.png")) }
					onMouseClicked = EventHandler {
						it.consume()
						Tabmanager.openTab(
							"WeekNotes${data.WeekofYear}/${data.time.year}", ::createnotetab, data
						)
					}
					
				}
			}
		}
		
		// appointments / weekdetails
		val pane = pane {
			style(append = true) {
//				backgroundColor += Color.RED
				// must be deactivated because style gets reset
				// when any (mouse)events happen
				prefHeight = detailspaneminHeight.px
//				minHeight = detailspaneminHeight.px
			}
		}
		
		if((data is Day && data.appointments.isNotEmpty()) || (data is Week && data.getallappointments().isNotEmpty())) {
			val thisexpandheight = when(data) {
				is Day -> generateAppointmentsGraphic(data, pane, animations)
				is Week -> generateWeekGraphic(data, pane, animations)
				else -> 0.0
			}
			
			if(expand.value < thisexpandheight)
				expand.value = thisexpandheight
		}
		
		var open = KeyValue(pane.minHeightProperty(), expand.value)
		var closeframe = KeyFrame(Duration(0.0), open)
		var openframe = KeyFrame(Duration(getConfig(Configs.Animationspeed)), open)
		
		expand.addListener(ChangeListener { _, _, new: Number ->
			opentimeline.keyFrames.remove(openframe)
			closetimeline.keyFrames.remove(closeframe)
			open = KeyValue(pane.minHeightProperty(), new)
			closeframe = KeyFrame(Duration(0.0), open)
			openframe = KeyFrame(Duration(getConfig(Configs.Animationspeed)), open)
			opentimeline.keyFrames.add(openframe)
			closetimeline.keyFrames.add(closeframe)
		})
		
		opentimeline.keyFrames.add(KeyFrame(Duration(0.0), KeyValue(pane.minHeightProperty(), detailspaneminHeight)))
		opentimeline.keyFrames.add(openframe)
		
		closetimeline.keyFrames.add(closeframe)
		closetimeline.keyFrames.add(
			KeyFrame(
				Duration(getConfig(Configs.Animationspeed)),
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
const val circlewidth = 8.0

const val sidetopmargin = 6.0
const val vtopmargin = 4.0
const val hleftmargin = 8.0

fun generateWeekGraphic(week: Week, pane: Pane, animations: Array<MutableList<Animation>>): Double {
	pane.clear()
	
	// make width even because ,5 pixel are not supported <-(AI said this)
	val width = (if(pane.width.toInt() % 2 == 0) pane.width.toInt() else pane.width.toInt() + 1).toDouble()
	
	val ycords = mutableListOf<Double>()
	for(index in 0 until week.getallappointmentssort().size) {
		ycords.add(sidetopmargin + index * (spacing * 2 + circlewidth))
	}
	
	for((index, appointmententry) in week.getallappointmentssort().entries.withIndex()) {
		pane.hbox(alignment = Pos.CENTER_LEFT, spacing = spacing) {
			circle(radius = circlewidth / 2) {
				fill = appointmententry.key.color
			}
			label("${appointmententry.value.size}:${appointmententry.key.name}") {
				addClass(Styles.CalendarTableView.cellappointtypelabel)
				maxWidth = width - hleftmargin - circlewidth
				ellipsisString = ".."
				textOverrun = OverrunStyle.ELLIPSIS
			}
			
			translateX = circlewidth / 2
			translateY = ycords[index] - circlewidth
			opacity = 0.0
		}
	}
	
	val openTransitions = mutableListOf<Animation>()
	val closeTransitions = mutableListOf<Animation>()
	
	for(label in pane.getChildList()?.filterIsInstance<HBox>()!!) {
		val openfadeTransition = Timeline(
			KeyFrame(Duration(0.0), KeyValue(label.opacityProperty(), 0.0)),
			KeyFrame(Duration(getConfig<Double>(Configs.Animationspeed) / 3 * 2), KeyValue(label.opacityProperty(), 0.0)),
			KeyFrame(Duration(getConfig(Configs.Animationspeed)), KeyValue(label.opacityProperty(), 1.0))
		)
		openTransitions.add(openfadeTransition)
		
		val closefadeTransition = Timeline(
			KeyFrame(Duration(0.0), KeyValue(label.opacityProperty(), 1.0)),
			KeyFrame(Duration(getConfig<Double>(Configs.Animationspeed) / 3), KeyValue(label.opacityProperty(), 0.0)),
			KeyFrame(Duration(getConfig(Configs.Animationspeed)), KeyValue(label.opacityProperty(), 0.0))
		)
		closeTransitions.add(closefadeTransition)
	}
	
	animations[0].clear()
	animations[0].addAll(openTransitions)
	animations[1].clear()
	animations[1].addAll(closeTransitions)
	
	return (ycords.getOrNull(ycords.lastIndex)?.plus(sidetopmargin) ?: 0.0)
	
}

fun generateAppointmentsGraphic(day: Day, pane: Pane, animations: Array<MutableList<Animation>>): Double {
	pane.clear()
	
	var appointments = day.getappointmentslimit()
	val limited = appointments.size != day.appointments.size
	if(limited)
		appointments = appointments.dropLast(1)
	
	// make width even because ,5 pixel are not supported <-(AI said this)
	val width = (if(pane.width.toInt() % 2 == 0) pane.width.toInt() else pane.width.toInt() + 1).toDouble()
	
	val topspacing: Double = maxOf(2.0, minOf(spacing, (((width - spacing) / appointments.size) / 3)))
	val topcirclewidth: Double = topspacing * 2
	
	val xcords = mutableListOf<Double>()
	
	if(appointments.size % 2 == 0) {
		for(index in 0 until appointments.size / 2) {
			xcords.add((width / 2) + ((topspacing / 2) + (index * (topcirclewidth + topspacing)) + topcirclewidth / 2))
			xcords.add((width / 2) - ((topspacing / 2) + (index * (topcirclewidth + topspacing)) + topcirclewidth / 2))
		}
	} else {
		xcords.add(width / 2)
		for(index in 0 until (appointments.size - 1) / 2) {
			xcords.add((width / 2) + ((topcirclewidth / 2) + topspacing + (index * (topcirclewidth + topspacing)) + topcirclewidth / 2))
			xcords.add((width / 2) - ((topcirclewidth / 2) + topspacing + (index * (topcirclewidth + topspacing)) + topcirclewidth / 2))
		}
	}
	
	// better animation because vertical are sorted from top to bottom
	xcords.sortDescending()
	
	for((index, appointment) in appointments.withIndex()) {
		pane.circle(radius = topcirclewidth / 2) {
			fill = appointment.type.color
			centerY = vtopmargin
			centerX = xcords[index]
		}
	}
	
	val ycords = mutableListOf<Double>()
	for(index in appointments.indices) {
		ycords.add(sidetopmargin + index * (spacing + circlewidth))
	}
	if(limited)
		ycords.add(sidetopmargin + ycords.size * (spacing + circlewidth))
	
	for((index, appointment) in appointments.withIndex()) {
		pane.label(appointment.description) {
			addClass(Styles.CalendarTableView.cellappointlabel)
			translateX = hleftmargin + circlewidth
			translateY = ycords[index] - circlewidth / 1.1
			opacity = 0.0
			
			maxWidth = width - hleftmargin - circlewidth
			ellipsisString = ".."
			textOverrun = OverrunStyle.ELLIPSIS
		}
	}
	
	if(limited) {
		pane.label("· · · · · · · · · · · · · · · · · · · · · · · ·") {
			addClass(Styles.CalendarTableView.cellappointtypelabel)
			style {
				fontWeight = FontWeight.BOLD
			}
			translateX = hleftmargin
			translateY = ycords[ycords.size - 1] - circlewidth //- spacing
			opacity = 0.0
			
			maxWidth = width - hleftmargin
			ellipsisString = ""
			textOverrun = OverrunStyle.ELLIPSIS
		}
	}
	
	val openTransitions = mutableListOf<Animation>()
	val closeTransitions = mutableListOf<Animation>()
	
	for(index in appointments.indices) {
		val circle = pane.getChildList()?.filterIsInstance<Circle>()?.get(index)
		val label = pane.getChildList()?.filterIsInstance<Label>()?.get(index)
		
		val openpath = Path()
		openpath.elements.add(MoveTo(xcords[index], sidetopmargin))
		openpath.elements.add(
			CubicCurveTo(
				xcords[index], sidetopmargin, hleftmargin * 1.8, sidetopmargin * 1.8, hleftmargin, ycords[index]
			),
		)
		openTransitions.add(PathTransition(Duration(getConfig(Configs.Animationspeed)), openpath, circle))
		
		val openfadeTransition = Timeline(
			KeyFrame(Duration(0.0), KeyValue(label?.opacityProperty(), 0.0)),
			KeyFrame(Duration(getConfig<Double>(Configs.Animationspeed) / 3 * 2), KeyValue(label?.opacityProperty(), 0.0)),
			KeyFrame(Duration(getConfig(Configs.Animationspeed)), KeyValue(label?.opacityProperty(), 1.0))
		)
		openTransitions.add(openfadeTransition)
		
		val closepath = Path()
		closepath.elements.add(MoveTo(hleftmargin, ycords[index]))
		closepath.elements.add(
			CubicCurveTo(
				hleftmargin, ycords[index], xcords[index], ycords[index], xcords[index], vtopmargin
			)
		)
		closeTransitions.add(PathTransition(Duration(getConfig(Configs.Animationspeed)), closepath, circle))
		
		val closefadeTransition = Timeline(
			KeyFrame(Duration(0.0), KeyValue(label?.opacityProperty(), 1.0)),
			KeyFrame(Duration(getConfig<Double>(Configs.Animationspeed) / 3), KeyValue(label?.opacityProperty(), 0.0)),
			KeyFrame(Duration(getConfig(Configs.Animationspeed)), KeyValue(label?.opacityProperty(), 0.0))
		)
		closeTransitions.add(closefadeTransition)
	}
	
	if(limited) {
		val label = pane.getChildList()?.filterIsInstance<Label>()?.last()
		val openfadeTransition = Timeline(
			KeyFrame(Duration(0.0), KeyValue(label?.opacityProperty(), 0.0)),
			KeyFrame(Duration(getConfig<Double>(Configs.Animationspeed) / 3 * 2), KeyValue(label?.opacityProperty(), 0.0)),
			KeyFrame(Duration(getConfig(Configs.Animationspeed)), KeyValue(label?.opacityProperty(), 1.0))
		)
		openTransitions.add(openfadeTransition)
		
		val closefadeTransition = Timeline(
			KeyFrame(Duration(0.0), KeyValue(label?.opacityProperty(), 1.0)),
			KeyFrame(Duration(getConfig<Double>(Configs.Animationspeed) / 3), KeyValue(label?.opacityProperty(), 0.0)),
			KeyFrame(Duration(getConfig(Configs.Animationspeed)), KeyValue(label?.opacityProperty(), 0.0))
		)
		closeTransitions.add(closefadeTransition)
	}
	
	animations[0].clear()
	animations[0].addAll(openTransitions)
	animations[1].clear()
	animations[1].addAll(closeTransitions)
	
	return (ycords.getOrNull(ycords.lastIndex)?.plus(sidetopmargin) ?: 0.0)
}
