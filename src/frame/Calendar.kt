package frame

import calendar.Celldisplay
import calendar.Day
import calendar.Week
import calendar.currentmonth
import calendar.currentmonthName
import calendar.setMonth
import javafx.animation.*
import javafx.beans.property.*
import javafx.collections.*
import javafx.event.*
import javafx.geometry.*
import javafx.scene.control.*
import javafx.scene.image.*
import javafx.scene.layout.*
import javafx.scene.paint.*
import javafx.scene.shape.*
import javafx.util.*
import logic.log
import tornadofx.*
import java.io.FileInputStream



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
						
						val selectedindex = SimpleIntegerProperty(-1)
						
						for((index, week) in list.withIndex()) {
							hbox(spacing = 5.0, alignment = Pos.CENTER) {
								columnlist.add(this@hbox)
								
								val opentimeline = Timeline()
								val closetimeline = Timeline()
								
								val cells = mutableListOf<VBox>()
								
								cells.add(createGraphics(week.general, this@hbox, opentimeline, closetimeline)[0] as VBox)
								
								val openappointmentopenanimations: MutableList<MutableList<Animation>> = mutableListOf()
								val closeappointmentopenanimations: MutableList<MutableList<Animation>> = mutableListOf()
								
								
								week.alldays.forEach {
									val tmp = createGraphics(it, this@hbox, opentimeline, closetimeline)
									cells.add(tmp[0] as VBox)
									
									@Suppress("UNCHECKED_CAST")
									openappointmentopenanimations.add(tmp[1] as MutableList<Animation>)
									@Suppress("UNCHECKED_CAST")
									closeappointmentopenanimations.add(tmp[2] as MutableList<Animation>)
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
									if(selectedindex.value != index) {
										openappointmentopenanimations.forEach { it.forEach { animation -> animation.play() } }
										opentimeline.play()
									}
								}
								
								onMouseExited = EventHandler {
									if(selectedindex.value != index) {
										openappointmentopenanimations.forEach { it.forEach { animation -> animation.stop() } }
										opentimeline.stop()
										closeappointmentopenanimations.forEach { it.forEach { animation -> animation.play() } }
										closetimeline.play()
									}
								}
								
								onMouseClicked = EventHandler {
									if(selectedindex.value != index) {
										selectedindex.value = index
										addClass(Styles.CalendarView.selectedcolumn)
									} else
										selectedindex.value = -1
								}
								
								selectedindex.addListener(ChangeListener { _, old, new ->
									if(new != index) {
										removeClass(Styles.CalendarView.selectedcolumn)
										if(old == index) {
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


fun createGraphics(data: Celldisplay, source: HBox, opentimeline: Timeline, closetimeline: Timeline): Array<Any?> {
	val animations: Array<MutableList<Animation>> = arrayOf(mutableListOf(), mutableListOf())
	val graphicContainer = source.vbox {
		addClass(Styles.CalendarView.tableitem)
		addClass(Styles.CalendarView.tablecell)
		
		if(data is Day) {
			hbox(alignment = Pos.CENTER) {
				imageview {
					style {
						minWidth = 15.px
						maxWidth = minWidth
						minHeight = 15.px
						maxHeight = minHeight
					}
					try {
						image = Image(FileInputStream("img/note.png"), 15.0, 15.0, true, true)
					} catch(e: IllegalArgumentException) {
						log(e)
					}
				}
				label(data.time.dayOfMonth.toString()) {
					addClass(Styles.CalendarView.celllabel)
				}
				imageview {
					style {
						minWidth = 15.px
						maxWidth = minWidth
						minHeight = 15.px
						maxHeight = minHeight
					}
					try {
						image = Image(FileInputStream("img/note.png"), 15.0, 15.0, true, true)
					} catch(e: IllegalArgumentException) {
						log(e)
					}
				}
			}
			
			// appointments
			val pane = pane {
				style {
					prefHeight = 10.px
				}
			}
			
			val openvalues = arrayOf( // duration, taskpane height
				arrayOf(0.0, 10.0),
				arrayOf(40.0, 30.0),
				arrayOf(160.0, 40.0),
				arrayOf(200.0, 55.0)
			)
			
			val closevalues = arrayOf( // duration, taskpane height
				arrayOf(200.0, 10.0),
				arrayOf(160.0, 30.0),
				arrayOf(40.0, 40.0),
				arrayOf(0.0, 55.0)
			)
			
			pane.let {
				for(value in openvalues) {
					opentimeline.keyFrames.add(
						KeyFrame(Duration(value[0]), KeyValue(it.minHeightProperty(), value[1].toInt()))
					)
				}
				
				for(value in closevalues) {
					closetimeline.keyFrames.add(
						KeyFrame(Duration(value[0]), KeyValue(it.minHeightProperty(), value[1].toInt()))
					)
				}
			}
			
			generateAppointmentsGraphic(data, pane, animations)
			
			pane.widthProperty().addListener { _ ->
				if(data.appointments.isEmpty())
					return@addListener
				
				generateAppointmentsGraphic(data, pane, animations)
			}
			add(pane)
		}
	}
	
	return arrayOf(graphicContainer, animations[0], animations[1])
}

fun generateAppointmentsGraphic(day: Day, pane: Pane, animations: Array<MutableList<Animation>>) {
	pane.clear()
	
	val width = pane.width.toInt().toDouble()
	val spacing = 5
	val circlewidth = 8
	
	val vtopmargin = 4.0
	val hleftmargin = 8.0
	
	val xcords = mutableListOf<Double>()
	
	if(day.appointments.size % 2 == 0) {
		for(index in 0 until day.appointments.size / 2) {
			xcords.add((width / 2) + ((spacing / 2) + (index * (circlewidth + spacing)) + circlewidth / 2))
			xcords.add((width / 2) + ((spacing / 2) + (index * (circlewidth + spacing)) + circlewidth / 2) * -1)
		}
	} else {
		xcords.add(width / 2)
		for(index in 0 until (day.appointments.size - 1) / 2) {
			xcords.add((width / 2) + ((circlewidth / 2) + spacing + (index * (circlewidth + spacing)) + circlewidth / 2))
			xcords.add((width / 2) + ((circlewidth / 2) + spacing + (index * (circlewidth + spacing)) + circlewidth / 2) * -1)
		}
	}
	
	// else from middle to center
	xcords.sortDescending()
	
	for((index, appointment) in day.appointments.withIndex()) {
		pane.circle(radius = circlewidth / 2) {
			fill = appointment._type.getColor()
			centerY = vtopmargin
			centerX = xcords[index]
		}
	}
	
	val ycords = mutableListOf<Double>()
	for(index in 0 until day.appointments.size) {
		ycords.add(8.0 + index * (spacing + circlewidth))
	}
	
	for((index, appointment) in day.appointments.withIndex()) {
		pane.label(appointment._description) {
			addClass(Styles.CalendarView.cellappointlabel)
			translateX = hleftmargin + circlewidth
			translateY = ycords[index] - circlewidth / 1.1
			maxWidth = width - hleftmargin - circlewidth
			opacity = 0.0
			
			ellipsisString = ".."
			textOverrun = OverrunStyle.ELLIPSIS
		}
	}
	
	val openTransitions = mutableListOf<Animation>()
	val closeTransitions = mutableListOf<Animation>()
	
	for(index in 0 until day.appointments.size) {
		val circle = pane.getChildList()?.filterIsInstance<Circle>()?.get(index)
		val label = pane.getChildList()?.filterIsInstance<Label>()?.get(index)
		
		val openpath = Path()
		openpath.elements.add(MoveTo(xcords[index], vtopmargin))
		openpath.elements.add(
			CubicCurveTo(
				xcords[index], vtopmargin, hleftmargin * 1.8, vtopmargin * 1.8, hleftmargin, ycords[index]
			),
		)
		openTransitions.add(PathTransition(Duration(300.0), openpath, circle))
		
		val openfadeTransition = Timeline(
			KeyFrame(Duration(0.0), KeyValue(label?.opacityProperty(), 0.0)),
			KeyFrame(Duration(150.0), KeyValue(label?.opacityProperty(), 0.0)),
			KeyFrame(Duration(300.0), KeyValue(label?.opacityProperty(), 1.0))
		)
		openTransitions.add(openfadeTransition)
		
		val closepath = Path()
		closepath.elements.add(MoveTo(hleftmargin, ycords[index]))
		closepath.elements.add(
			CubicCurveTo(
				hleftmargin, ycords[index], xcords[index], ycords[index], xcords[index], vtopmargin
			)
		)
		closeTransitions.add(PathTransition(Duration(200.0), closepath, circle))
		
		val closefadeTransition = Timeline(
			KeyFrame(Duration(0.0), KeyValue(label?.opacityProperty(), 1.0)),
			KeyFrame(Duration(100.0), KeyValue(label?.opacityProperty(), 0.0)),
			KeyFrame(Duration(300.0), KeyValue(label?.opacityProperty(), 0.0))
		)
		closeTransitions.add(closefadeTransition)
	}
	
	animations[0].clear()
	animations[0].addAll(openTransitions)
	animations[1].clear()
	animations[1].addAll(closeTransitions)
}
