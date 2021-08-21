package frame

import calendar.Celldisplay
import calendar.Day
import calendar.Types
import calendar.Week
import calendar.currentmonth
import calendar.currentmonthName
import calendar.setMonth
import javafx.animation.*
import javafx.collections.*
import javafx.event.*
import javafx.geometry.*
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.paint.*
import javafx.scene.shape.*
import javafx.util.*
import tornadofx.*

// Animations

val openvalues = arrayOf( // duration, cell height, taskpane height
	arrayOf(0.0, 46, 10),
	arrayOf(40.0, 56, 30),
	arrayOf(160.0, 80, 40),
	arrayOf(200.0, 90, 55)
)

val closevalues = arrayOf( // duration, cell height, taskpane height
	arrayOf(200.0, 46, 10),
	arrayOf(160.0, 56, 30),
	arrayOf(40.0, 80, 40),
	arrayOf(0.0, 90, 55)
)

fun createGraphics(data: Celldisplay, source: HBox, opentimeline: Timeline, closetimeline: Timeline): VBox {
	val graphicContainer = source.vbox {
		addClass(Styles.CalendarView.tableitem)
		addClass(Styles.CalendarView.tablecell)
		
		if(data is Day) {
			label(data.time.dayOfMonth.toString()) {
				addClass(Styles.CalendarView.celllabel)
			}
			val pane = pane {
				style {
					//backgroundColor += Color.RED
					prefHeight = 10.px
				}
				
			}
			
			pane.let {
				for(value in openvalues) {
					val keyvalues = mutableListOf<KeyValue>()
					keyvalues.add(KeyValue(it.minHeightProperty(), value[2] as Number))
					opentimeline.keyFrames.add(
						KeyFrame(Duration(value[0] as Double), *keyvalues.toTypedArray())
					)
				}
				
				for(value in closevalues) {
					val keyvalues = mutableListOf<KeyValue>()
					keyvalues.add(KeyValue(it.minHeightProperty(), value[2] as Number))
					closetimeline.keyFrames.add(
						KeyFrame(Duration(value[0] as Double), *keyvalues.toTypedArray())
					)
				}
			}
			
			pane.widthProperty().addListener { _ ->
				if(data.appointments.isEmpty())
					return@addListener
				
				val animations = generateAppointmentsGraphic(data, pane)
				data.appointmentopenanimations.clear()
				data.appointmentopenanimations.addAll(animations[0])
				data.appointmentcloseanimations.clear()
				data.appointmentcloseanimations.addAll(animations[1])
			}
			add(pane)
		}
	}
	
	return graphicContainer
}

fun generateAppointmentsGraphic(day: Day, pane: Pane): Array<MutableList<PathTransition>> {
	pane.clear()
	
	val xcords = mutableListOf<Double>()
	
	val width = pane.width.toInt().toDouble()
	val spacing = 5
	val circlewidth = 6
	
	val vtopmargin = 4.0
	val hleftmargin = 8.0
	
	if(day.appointments.size%2 == 0) {
		for(index in 0 until day.appointments.size/2) {
			xcords.add((width/2) + ((spacing/2) + (index*(circlewidth + spacing)) + circlewidth/2))
			xcords.add((width/2) + ((spacing/2) + (index*(circlewidth + spacing)) + circlewidth/2)*-1)
		}
	} else {
		xcords.add(width/2)
		for(index in 0 until (day.appointments.size - 1)/2) {
			xcords.add((width/2) + ((circlewidth/2) + spacing + (index*(circlewidth + spacing)) + circlewidth/2))
			xcords.add((width/2) + ((circlewidth/2) + spacing + (index*(circlewidth + spacing)) + circlewidth/2)*-1)
		}
	}
	
	// else from middle to center
	xcords.sortDescending()
	
	for((index, appointment) in day.appointments.withIndex()) {
		pane.add(pane.circle(radius = circlewidth/2) {
			fill = colormap[appointment._type]
			centerY = vtopmargin
			centerX = xcords[index]
		})
	}
	
	val ycords = mutableListOf<Double>()
	for(index in 0 until day.appointments.size) {
		ycords.add(8.0 + index*(spacing + circlewidth))
	}
	
	val openTransitions = mutableListOf<PathTransition>()
	val closeTransitions = mutableListOf<PathTransition>()
	
	for(index in 0 until day.appointments.size) {
		val openpath = Path()
		openpath.elements.add(MoveTo(xcords[index], vtopmargin))
		openpath.elements.add(
			CubicCurveTo(
				xcords[index], vtopmargin, hleftmargin*1.8, vtopmargin*1.8, hleftmargin, ycords[index]
			)
		)
		openTransitions.add(PathTransition(Duration(300.0), openpath, pane.getChildList()?.get(index)))
		
		val closepath = Path()
		closepath.elements.add(MoveTo(hleftmargin, ycords[index]))
		closepath.elements.add(
			CubicCurveTo(
				hleftmargin, ycords[index], xcords[index], ycords[index], xcords[index], vtopmargin
			)
		)
		closeTransitions.add(PathTransition(Duration(200.0), closepath, pane.getChildList()?.get(index)))
	}
	return arrayOf(openTransitions, closeTransitions)
}

val colormap: Map<Types, Color> = mapOf(
	Types.Work to Color.CYAN,
	Types.School to Color.GOLD,
)

fun createcalendartab(pane: TabPane): Tab {
	return pane.tab("Calender") {
		isClosable = false
		
		stackpane {
			style(append = true) {
				maxHeight = 500.px
				padding = box(6.px)
			}
			
			// maintab
			vbox {
				style {
					borderColor += box(Color.TRANSPARENT)
					borderWidth += box(5.px)
				}
				
				hbox {
					alignment = Pos.CENTER
					spacing = 40.0
					style {
						minHeight = 50.px
						maxHeight = 50.px
						backgroundColor += Color.DODGERBLUE
					}
					button("<") {
						addClass(Styles.CalendarView.titlebuttons)
						action {
							setMonth(false)
						}
					}
					label(currentmonthName) {
						addClass(Styles.CalendarView.title)
						minWidth = 200.0
						alignment = Pos.CENTER
					}
					button(">") {
						addClass(Styles.CalendarView.titlebuttons)
						action {
							setMonth(true)
						}
					}
				}
				
				// seperator
				label {
					style {
						backgroundColor += Color.BLACK
						prefHeight = 2.px
						maxHeight = 2.px
						minHeight = 2.px
					}
					useMaxWidth = true
				}
				
				vbox(spacing = 5.0, alignment = Pos.TOP_CENTER) {
					addClass(Styles.CalendarView.table)
					style(append = true) {
						backgroundColor += Color.WHITE
						padding = box(3.px)
					}
					
					// Top bar
					hbox(spacing = 5.0, alignment = Pos.CENTER) {
						label("") {
							addClass(Styles.CalendarView.tableitem)
						}
						label("Monday") {
							addClass(Styles.CalendarView.tableitem)
							addClass(Styles.CalendarView.tableheader)
							addClass(Styles.CalendarView.cellheaderlabel)
						}
						label("Tuesday") {
							addClass(Styles.CalendarView.tableitem)
							addClass(Styles.CalendarView.tableheader)
							addClass(Styles.CalendarView.cellheaderlabel)
						}
						label("Wednesday") {
							addClass(Styles.CalendarView.tableitem)
							addClass(Styles.CalendarView.tableheader)
							addClass(Styles.CalendarView.cellheaderlabel)
						}
						label("Thursday") {
							addClass(Styles.CalendarView.tableitem)
							addClass(Styles.CalendarView.tableheader)
							addClass(Styles.CalendarView.cellheaderlabel)
						}
						label("Friday") {
							addClass(Styles.CalendarView.tableitem)
							addClass(Styles.CalendarView.tableheader)
							addClass(Styles.CalendarView.cellheaderlabel)
						}
						label("Saturday") {
							addClass(Styles.CalendarView.tableitem)
							addClass(Styles.CalendarView.tableheader)
							addClass(Styles.CalendarView.cellheaderlabel)
						}
						label("Sunday") {
							addClass(Styles.CalendarView.tableitem)
							addClass(Styles.CalendarView.tableheader)
							addClass(Styles.CalendarView.cellheaderlabel)
						}
					}
					
					// remove existing columns without removing header
					val columnlist: MutableList<HBox> = mutableListOf()
					
					fun updateTable(list: ObservableList<out Week>) {
						children.removeAll(columnlist)
						columnlist.clear()
						
						for(week in list) {
							hbox(spacing = 5.0, alignment = Pos.CENTER) {
								columnlist.add(this@hbox)
								
								val opentimeline = Timeline()
								val closetimeline = Timeline()
								
								val cells = mutableListOf<VBox>()
								
								cells.add(createGraphics(week.general, this@hbox, opentimeline, closetimeline))
								week.alldays.forEach {
									cells.add(createGraphics(it, this@hbox, opentimeline, closetimeline))
								}
								
								cells.forEach {
									it.onMouseEntered = EventHandler { _ ->
										it.addClass(Styles.CalendarView.hoveredtablecell)
									}
									it.onMouseExited = EventHandler { _ ->
										it.removeClass(Styles.CalendarView.hoveredtablecell)
									}
								}
								
								onMouseEntered = EventHandler {
									week.alldays.forEach { it.appointmentopenanimations.forEach { animation -> animation.play() } }
									opentimeline.play()
								}
								
								onMouseExited = EventHandler {
									week.alldays.forEach { it.appointmentopenanimations.forEach { animation -> animation.stop() } }
									opentimeline.stop()
									week.alldays.forEach { it.appointmentcloseanimations.forEach { animation -> animation.play() } }
									closetimeline.play()
								}
								
							}
						}
					}
					
					currentmonth.addListener(ListChangeListener {
						updateTable(it.list)
					})
					
					updateTable(currentmonth)
				}
			}
			
			// used to shadow the overflow from actual tap
			pane {
				isMouseTransparent = true
				style {
					borderColor += box(Color.BLACK)
					borderWidth += box(5.px)
					borderRadius += box(10.px)
				}
			}
		}
	}
}