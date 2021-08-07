package frame

import javafx.animation.Animation
import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.animation.PathTransition
import javafx.animation.Timeline
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.shape.CubicCurveTo
import javafx.scene.shape.MoveTo
import javafx.scene.shape.Path
import javafx.util.Callback
import javafx.util.Duration
import logic.getLangString
import tornadofx.action
import tornadofx.add
import tornadofx.addClass
import tornadofx.box
import tornadofx.button
import tornadofx.circle
import tornadofx.clear
import tornadofx.column
import tornadofx.getChildList
import tornadofx.hbox
import tornadofx.insets
import tornadofx.label
import tornadofx.pane
import tornadofx.px
import tornadofx.removeClass
import tornadofx.stackpane
import tornadofx.style
import tornadofx.tab
import tornadofx.tableview
import tornadofx.useMaxWidth
import tornadofx.vbox
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime


val now: ZonedDateTime = ZonedDateTime.now(ZoneId.systemDefault())

val currentmonth: ObservableList<Week> = FXCollections.observableArrayList()

fun main() {
	val data = GetMonth(now.month.value)
	currentmonth.clear()
	currentmonth.addAll(data)

//	val fout = FileOutputStream("")
//	val dout = DataOutputStream(fout)

//	fout.close()
//	dout.close()

}


fun GetMonth(month: Int): MutableList<Week> {
	var Time: ZonedDateTime = now.withMonth(month).withDayOfMonth(1)

	val dayoffset = Time.dayOfWeek.value
	Time = Time.minusDays((dayoffset - 1).toLong())

	val weeks: MutableList<Week> = mutableListOf()

	do {
		val newWeek = Week()

		do {
			when(Time.dayOfWeek) {
				DayOfWeek.MONDAY -> newWeek.Monday = Day(Time.plusHours(0))
				DayOfWeek.TUESDAY -> newWeek.Tuesday = Day(Time.plusHours(0))
				DayOfWeek.WEDNESDAY -> newWeek.Wednesday = Day(Time.plusHours(0))
				DayOfWeek.THURSDAY -> newWeek.Thursday = Day(Time.plusHours(0))
				DayOfWeek.FRIDAY -> newWeek.Friday = Day(Time.plusHours(0))
				DayOfWeek.SATURDAY -> newWeek.Saturday = Day(Time.plusHours(0))
				DayOfWeek.SUNDAY -> newWeek.Sunday = Day(Time.plusHours(0))
				else -> continue
			}
			Time = Time.plusDays(1)
		} while(Time.dayOfWeek.value != 1)

		newWeek.Friday.appointments = listOf(
			Appointment("Arb", Types.Work),
			Appointment("School", Types.School),
			Appointment("School", Types.School),
			Appointment("Arb 3", Types.Work)
		)
		newWeek.Saturday.appointments = listOf(Appointment("Arb 2", Types.Work), Appointment("Arb 3", Types.Work))
		newWeek.Thursday.appointments = listOf(Appointment("Mathe", Types.School))

		weeks.add(weeks.size, newWeek)
	} while(Time.month.value == month && Time.dayOfMonth > 1)

	return weeks
}

fun createGraphics(celldata: Any?, graphic: TableCell<Week, Any>): Array<Any?>? {
	if(celldata == null)
		return null

	var generateddata: Array<Any>? = null

	val graphicContainer = graphic.vbox {
		addClass(Styles.CalendarView.tablecellpane)
		style(append = true) {
			alignment = Pos.TOP_CENTER
		}

		if(celldata is Day) {
			label {
				addClass(Styles.CalendarView.celllabel)
				text = celldata.toString()
			}
			generateddata = generateDayGraphic(celldata, this)
			add(generateddata !![0] as Node)
		}
	}
	return arrayOf(graphicContainer, generateddata?.get(1), generateddata?.get(2))
}

fun generateDayGraphic(day: Day, vBox: VBox): Array<Any> {
	val pane = vBox.pane {
		style {
			//backgroundColor += Color.RED
			prefHeight = 10.px
		}
	}

	var first = true

	val openTransitions = mutableListOf<PathTransition>()
	val closeTransitions = mutableListOf<PathTransition>()

	pane.widthProperty().addListener {_ ->
		if(! first || day.appointments.isEmpty())
			return@addListener

		first = false
		pane.clear()

		val xcords = mutableListOf<Double>()

		val width = pane.width.toInt().toDouble()
		val spacing = 5
		val circlewidth = 6

		val vtopmargin = 4.0
		val hleftmargin = 8.0

		if(day.appointments.size % 2 == 0) {
			for(index in 0 until day.appointments.size / 2) {
				xcords.add((width / 2) + ((spacing / 2) + (index * (circlewidth + spacing)) + circlewidth / 2))
				xcords.add((width / 2) + ((spacing / 2) + (index * (circlewidth + spacing)) + circlewidth / 2) * - 1)
			}
		} else {
			xcords.add(width / 2)
			for(index in 0 until (day.appointments.size - 1) / 2) {
				xcords.add((width / 2) + ((circlewidth / 2) + spacing + (index * (circlewidth + spacing)) + circlewidth / 2))
				xcords.add((width / 2) + ((circlewidth / 2) + spacing + (index * (circlewidth + spacing)) + circlewidth / 2) * - 1)
			}
		}

		// else from middle to center
		xcords.sortDescending()

		for((index, appointment) in day.appointments.withIndex()) {
			pane.add(pane.circle(radius = circlewidth / 2) {
				fill = colormap[appointment._type]
				centerY = vtopmargin
				centerX = xcords[index]
			})
		}

		val ycords = mutableListOf<Double>()
		for(index in 0 until day.appointments.size) {
			ycords.add(8.0 + index * (spacing + circlewidth))
		}

		for(index in 0 until day.appointments.size) {
			val openpath = Path()
			openpath.elements.add(MoveTo(xcords[index], vtopmargin))
			openpath.elements.add(
				CubicCurveTo(
					xcords[index], vtopmargin, hleftmargin * 1.8, vtopmargin * 1.8, hleftmargin, ycords[index]
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
	}

	return arrayOf(pane, openTransitions, closeTransitions)
}

val colormap: Map<Types, Color> = mapOf(
	Types.Work to Color.CYAN,
	Types.School to Color.GOLD,
)

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

fun <T> cellfactory(): Callback<TableColumn<Week, T>, TableCell<Week, T>> {
	return Callback<TableColumn<Week, T>, TableCell<Week, T>> {
		val cell: TableCell<Week, T> = TableCell()

		val opentimeline = Timeline()
		var open: MutableList<Animation> = mutableListOf()
		val closetimeline = Timeline()
		var close: MutableList<Animation> = mutableListOf()

		cell.itemProperty().addListener {_, _, day2 ->
			createGraphics(day2, cell as TableCell<Week, Any>)?.also {graphicdata ->
				cell.graphic = graphicdata[0] as Node?
				cell.addClass(Styles.CalendarView.tablecell)

				open.clear()
				close.clear()

				val pane = (graphicdata[0] as Node).getChildList()?.filterIsInstance<Pane>()?.firstOrNull()

				pane?.let {
					for(value in openvalues) {
						val keyvalues = mutableListOf<KeyValue>()
						keyvalues.add(KeyValue(cell.minHeightProperty(), value[1] as Number))
						keyvalues.add(KeyValue(it.minHeightProperty(), value[2] as Number))

						opentimeline.keyFrames.add(
							KeyFrame(
								Duration(value[0] as Double), *keyvalues.toTypedArray()
							)
						)
					}

					for(value in closevalues) {
						val keyvalues = mutableListOf<KeyValue>()
						keyvalues.add(KeyValue(cell.minHeightProperty(), value[1] as Number))
						keyvalues.add(KeyValue(it.minHeightProperty(), value[2] as Number))
						closetimeline.keyFrames.add(
							KeyFrame(Duration(value[0] as Double), *keyvalues.toTypedArray())
						)
					}

					open = graphicdata[1] as MutableList<Animation>
					close = graphicdata[2] as MutableList<Animation>
				}
			}
		}

		cell.onMouseEntered = EventHandler {
			(it.target as Node).getChildList()?.get(0)?.addClass(Styles.CalendarView.hoveredtablecellpane)
			(it.target as Node).addClass(Styles.CalendarView.hoveredtablecell)
			open.forEach {animation -> animation.play()}
			opentimeline.play()

		}
		cell.onMouseExited = EventHandler {
			(it.target as Node).getChildList()?.get(0)?.removeClass(Styles.CalendarView.hoveredtablecellpane)
			(it.target as Node).removeClass(Styles.CalendarView.hoveredtablecell)
			open.forEach {animation -> animation.stop()}
			opentimeline.stop()
			close.forEach {animation -> animation.play()}
			closetimeline.play()
		}
		return@Callback cell
	}
}

fun createcalendartab(pane: TabPane): Tab {
	return pane.tab("Calender") {
		isClosable = false
		stackpane {
			style(append = true) {
				maxHeight = 435.px
			}
			padding = insets(6)
			vbox {
				style {
					borderColor += box(Color.TRANSPARENT)
					borderWidth += box(5.px)
					orientation = Orientation.VERTICAL
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

						}
					}
					label("December") {
						addClass(Styles.CalendarView.title)
					}
					button(">") {
						addClass(Styles.CalendarView.titlebuttons)
						action {

						}
					}
				}

				label {
					style {
						backgroundColor += Color.BLACK
						prefHeight = 2.px
						maxHeight = 2.px
						minHeight = 2.px
					}
					useMaxWidth = true
				}

				tableview(currentmonth) {
					addClass(Styles.CalendarView.table)

					columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY

					column("", Week::self) {
						addClass(Styles.CalendarView.column)
						isSortable = false
						isReorderable = false
						cellFactory = cellfactory()
					}
					column(getLangString("Monday"), Week::Monday) {
						addClass(Styles.CalendarView.column)
						isSortable = false
						isReorderable = false
						cellFactory = cellfactory()
					}
					column(getLangString("Tuesday"), Week::Tuesday) {
						addClass(Styles.CalendarView.column)
						isSortable = false
						isReorderable = false
						cellFactory = cellfactory()
					}
					column(getLangString("Wednesday"), Week::Wednesday) {
						addClass(Styles.CalendarView.column)
						isSortable = false
						isReorderable = false
						cellFactory = cellfactory()
					}
					column(getLangString("Thursday"), Week::Thursday) {
						addClass(Styles.CalendarView.column)
						isSortable = false
						isReorderable = false
						cellFactory = cellfactory()
					}
					column(getLangString("Friday"), Week::Friday) {
						addClass(Styles.CalendarView.column)
						isSortable = false
						isReorderable = false
						cellFactory = cellfactory()
					}
					column(getLangString("Saturday"), Week::Saturday) {
						addClass(Styles.CalendarView.column)
						isSortable = false
						isReorderable = false
						cellFactory = cellfactory()
					}
					column(getLangString("Sunday"), Week::Sunday) {
						addClass(Styles.CalendarView.column)
						isSortable = false
						isReorderable = false
						cellFactory = cellfactory()
					}
				}
			}
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