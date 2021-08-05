package frame

import javafx.animation.KeyFrame
import javafx.animation.KeyValue
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
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.util.Callback
import javafx.util.Duration
import logic.getLangString
import tornadofx.action
import tornadofx.addClass
import tornadofx.box
import tornadofx.button
import tornadofx.circle
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

		weeks.add(weeks.size, newWeek)
	} while(Time.month.value == month && Time.dayOfMonth > 1)

	return weeks
}

fun createGraphics(celldata: Any?, graphic: Node): Pane? {
	if(celldata == null)
		return null
	val graphicContainer = graphic.vbox {
		addClass(Styles.CalendarView.tablecellpane)
		style(append = true) {
			alignment = Pos.TOP_CENTER
		}

		label {
			addClass(Styles.CalendarView.celllabel)
			if(celldata is Day) {
				text = celldata.toString()
			}
		}

		if(celldata is Day) {
			hbox {
				style(append = true) {
					padding = box(4.px)
					spacing = 5.px
					alignment = Pos.CENTER
				}
				circle(radius = 5.0) {
					style(append = true) {
						fill = Color.RED
					}
				}
				circle(radius = 5.0) {
					style(append = true) {
						baseColor = Color.RED
					}
				}
			}
		}
	}
	return graphicContainer
}


fun <T> cellfactory(): Callback<TableColumn<Week, T>, TableCell<Week, T>> {
	return Callback<TableColumn<Week, T>, TableCell<Week, T>> {
		val cell: TableCell<Week, T> = TableCell()

		val open = Timeline()
		val close = Timeline()

		cell.itemProperty().addListener {_, _, day2 ->
			createGraphics(day2, cell)?.also {it ->
				cell.graphic = it
				cell.addClass(Styles.CalendarView.tablecell)

				open.keyFrames.clear()
				close.keyFrames.clear()

				val circles = it.getChildList()?.filterIsInstance<HBox>()?.let {
					if (it.isNotEmpty())
						return@let it[0].getChildList()
					else
						return@let listOf()
				} ?: listOf()

				val openvalues = arrayOf( // duration,height,opacity
					arrayOf(0.0, 46, 1.0),
					arrayOf(20.0, 56, 0.5),
					arrayOf(80.0, 80, 0.2),
					arrayOf(100.0, 90, 0.0)
				)

				val closevalues = arrayOf( // duration,height,opacity
					arrayOf(100.0, 46, 1.0),
					arrayOf(80.0, 56, 0.5),
					arrayOf(20.0, 80, 0.2),
					arrayOf(0.0, 90, 0.0)
				)

				for(value in openvalues) {
					val keyvalues = mutableListOf<KeyValue>()
					keyvalues.add(KeyValue(cell.minHeightProperty(), value[1] as Number))
					for(circle in circles) {
						keyvalues.add(KeyValue(circle.opacityProperty(), value[2] as Double))
					}
					open.keyFrames.add(
						KeyFrame(
							Duration(value[0] as Double), *keyvalues.toTypedArray()
						)
					)
				}

				for(value in closevalues) {
					val keyvalues = mutableListOf<KeyValue>()
					keyvalues.add(KeyValue(cell.minHeightProperty(), value[1] as Number))
					for(circle in circles) {
						keyvalues.add(KeyValue(circle.opacityProperty(), value[2] as Double))
					}
					close.keyFrames.add(
						KeyFrame(
							Duration(value[0] as Double), *keyvalues.toTypedArray()
						)
					)
				}
			}
		}

		cell.onMouseEntered = EventHandler {
			(it.target as Node).getChildList()?.get(0)?.addClass(Styles.CalendarView.hoveredtablecellpane)
			(it.target as Node).addClass(Styles.CalendarView.hoveredtablecell)
			open.play()
		}
		cell.onMouseExited = EventHandler {
			(it.target as Node).getChildList()?.get(0)?.removeClass(Styles.CalendarView.hoveredtablecellpane)
			(it.target as Node).removeClass(Styles.CalendarView.hoveredtablecell)
			close.play()
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