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
import logic.Configs
import logic.getConfig
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
				
				// Table view
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
								
								cells.add(createCellGraphics(week.general, this@hbox, opentimeline, closetimeline)[0] as VBox)
								
								val openappointmentopenanimations: MutableList<MutableList<Animation>> = mutableListOf()
								val closeappointmentopenanimations: MutableList<MutableList<Animation>> = mutableListOf()
								
								
								week.alldays.forEach {
									val tmp = createCellGraphics(it, this@hbox, opentimeline, closetimeline)
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
								
								var openprep = false
								
								onMouseEntered = EventHandler {
									if(selectedindex.value != index) {
										openprep = true
										Thread {
											Thread.sleep(getConfig<Double>(Configs.Animationdelay).toLong())
											if(openprep) {
												openprep = false
												openappointmentopenanimations.forEach { it.forEach { animation -> animation.play() } }
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
											openappointmentopenanimations.forEach { it.forEach { animation -> animation.stop() } }
											opentimeline.stop()
											closeappointmentopenanimations.forEach { it.forEach { animation -> animation.play() } }
											closetimeline.play()
										}
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
										if(old == index && new != -1) {
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
				style {
					borderColor += box(Color.BLACK)
					borderWidth += box(5.px)
					borderRadius += box(10.px)
				}
			}
		}
	}
}

fun createCellGraphics(data: Celldisplay, source: HBox, opentimeline: Timeline, closetimeline: Timeline): Array<Any?> {
	val animations: Array<MutableList<Animation>> = arrayOf(mutableListOf(), mutableListOf())
	val graphicContainer = source.vbox {
		addClass(Styles.CalendarView.tableitem)
		addClass(Styles.CalendarView.tablecell)
		
		if(data is Day) {
			gridpane {
				style {
					prefWidth = Int.MAX_VALUE.px
					padding = box(3.px, 3.px, 0.px, 3.px)
				}
				imageview {
					gridpaneConstraints {
						columnRowIndex(0, 0)
					}
					image = Image(FileInputStream("img/remind.png"))
				}
				
				label(data.time.dayOfMonth.toString()) {
					gridpaneConstraints {
						columnRowIndex(1, 0)
					}
					addClass(Styles.CalendarView.celllabel)
				}
				
				imageview {
					gridpaneConstraints {
						columnRowIndex(2, 0)
					}
					image = Image(FileInputStream("img/note.png"))
				}
			}
			
			// appointments
			val pane = pane {
				style {
					prefHeight = 10.px
				}
			}
			
			opentimeline.keyFrames.add(KeyFrame(Duration(0.0), KeyValue(pane.minHeightProperty(), 10)))
			opentimeline.keyFrames.add(KeyFrame(Duration(getConfig(Configs.Animationspeed)), KeyValue(pane.minHeightProperty(), 55)))
			
			closetimeline.keyFrames.add(KeyFrame(Duration(0.0), KeyValue(pane.minHeightProperty(), 55)))
			closetimeline.keyFrames.add(KeyFrame(Duration(getConfig(Configs.Animationspeed)), KeyValue(pane.minHeightProperty(), 10)))
			
			generateAppointmentsGraphic(data, pane, animations)
			
			pane.widthProperty().addListener { _ ->
				if(data.appointments.isEmpty())
					return@addListener
				generateAppointmentsGraphic(data, pane, animations)
			}
			
		} else if(data is Week) {
			gridpane {
				style {
					prefWidth = Int.MAX_VALUE.px
					padding = box(3.px, 3.px, 0.px, 3.px)
				}
				
				label(data.WeekofYear.toString()) {
					gridpaneConstraints {
						columnRowIndex(1, 0)
					}
					addClass(Styles.CalendarView.celllabel)
				}
				
				imageview {
					gridpaneConstraints {
						columnRowIndex(2, 0)
					}
					image = Image(FileInputStream("img/note.png"))
				}
			}
			
			val pane = pane {
				style {
					prefHeight = 10.px
				}
			}
			
			opentimeline.keyFrames.add(KeyFrame(Duration(0.0), KeyValue(pane.minHeightProperty(), 10)))
			opentimeline.keyFrames.add(KeyFrame(Duration(getConfig(Configs.Animationspeed)), KeyValue(pane.minHeightProperty(), 55)))
			
			closetimeline.keyFrames.add(KeyFrame(Duration(0.0), KeyValue(pane.minHeightProperty(), 55)))
			closetimeline.keyFrames.add(KeyFrame(Duration(getConfig(Configs.Animationspeed)), KeyValue(pane.minHeightProperty(), 10)))
			
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
	
	animations[0].clear()
	animations[0].addAll(openTransitions)
	animations[1].clear()
	animations[1].addAll(closeTransitions)
}
