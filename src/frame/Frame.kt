package frame


import javafx.event.EventHandler
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.stage.Stage
import javafx.util.Callback
import logic.getLangString
import tornadofx.App
import tornadofx.action
import tornadofx.addClass
import tornadofx.borderpane
import tornadofx.box
import tornadofx.button
import tornadofx.column
import tornadofx.customitem
import tornadofx.gridpane
import tornadofx.gridpaneConstraints
import tornadofx.hbox
import tornadofx.insets
import tornadofx.label
import tornadofx.launch
import tornadofx.menu
import tornadofx.menubar
import tornadofx.pane
import tornadofx.px
import tornadofx.separator
import tornadofx.stackpane
import tornadofx.style
import tornadofx.tab
import tornadofx.tableview
import tornadofx.tabpane
import tornadofx.useMaxWidth
import tornadofx.vbox


//https://edvin.gitbooks.io/tornadofx-guide/content/part1/7_Layouts_and_Menus.html

class Application: App(MainView::class, Styles::class) {

	override fun start(stage: Stage) {
		stage.height = 500.0
		stage.width = 700.0
		super.start(stage)
	}
}

fun frameInit() {
	main()
	launch<Application>()
}

class MainView: tornadofx.View("Calendar") {
	override val root = borderpane {
		top = menubar {
			menu(getLangString("options")) {
				val itemwidth = 120.px
				customitem {
					gridpane {
						addClass(Styles.Menubar.gridpane)
						style(append = true) {
							prefWidth = itemwidth
						}
						label(getLangString("Reload")) {
							addClass(Styles.Menubar.itemname)
							gridpaneConstraints {
								columnIndex = 0
							}
						}
						label {
							gridpaneConstraints {
								columnIndex = 1
								hGrow = Priority.ALWAYS
							}
							style(append = true) {
								prefWidth = 10.px
							}
						}
						label("F5") {
							addClass(Styles.Menubar.itemshortcut)
							gridpaneConstraints {
								columnIndex = 2

								hGrow = Priority.SOMETIMES
							}
						}
					}
				}
				customitem {
					gridpane {
						addClass(Styles.Menubar.gridpane)
						style(append = true) {
							prefWidth = itemwidth
						}
						label(getLangString("Preferences")) {
							addClass(Styles.Menubar.itemname)
							gridpaneConstraints {
								columnIndex = 0
							}
						}
						label {
							gridpaneConstraints {
								columnIndex = 1
								hGrow = Priority.ALWAYS
							}
							style(append = true) {
								prefWidth = 10.px
							}
						}
						label("Strg + ,") {
							addClass(Styles.Menubar.itemshortcut)
							gridpaneConstraints {
								columnIndex = 2

								hGrow = Priority.SOMETIMES
							}
						}
					}
				}
				separator()
				customitem {
					gridpane {
						addClass(Styles.Menubar.gridpane)
						style(append = true) {
							prefWidth = itemwidth
						}
						label(getLangString("Quit")) {
							addClass(Styles.Menubar.itemname)
							gridpaneConstraints {
								columnIndex = 0
							}
						}
						label {
							gridpaneConstraints {
								columnIndex = 1
								hGrow = Priority.ALWAYS
							}
							style(append = true) {
								prefWidth = 10.px
							}
						}
						label("Strg + Q") {
							addClass(Styles.Menubar.itemshortcut)
							gridpaneConstraints {
								columnIndex = 2

								hGrow = Priority.SOMETIMES
							}
						}
					}
				}
			}
			menu(getLangString("view")) {
				menu(getLangString("show")) {
					val itemwidth = 170.px
					customitem {
						gridpane {
							addClass(Styles.Menubar.gridpane)
							style(append = true) {
								prefWidth = itemwidth
							}
							label(getLangString("Show Reminder")) {
								addClass(Styles.Menubar.itemname)
								gridpaneConstraints {
									columnIndex = 0
								}
							}
							label {
								gridpaneConstraints {
									columnIndex = 1
									hGrow = Priority.ALWAYS
								}
								style(append = true) {
									prefWidth = 10.px
								}
							}
							label("Strg + Shift + R") {
								addClass(Styles.Menubar.itemshortcut)
								gridpaneConstraints {
									columnIndex = 2

									hGrow = Priority.SOMETIMES
								}
							}
						}
					}
					customitem {
						gridpane {
							addClass(Styles.Menubar.gridpane)
							style(append = true) {
								prefWidth = itemwidth
							}
							label(getLangString("Show Calendar")) {
								addClass(Styles.Menubar.itemname)
								gridpaneConstraints {
									columnIndex = 0
								}
							}
							label {
								gridpaneConstraints {
									columnIndex = 1
									hGrow = Priority.ALWAYS
								}
								style(append = true) {
									prefWidth = 10.px
								}
							}
							label("Strg + Shift + C") {
								addClass(Styles.Menubar.itemshortcut)
								gridpaneConstraints {
									columnIndex = 2
									hGrow = Priority.SOMETIMES
								}
							}
						}
					}
				}
			}
			menu(getLangString("help")) {
				val itemwidth = 80.px
				customitem {
					gridpane {
						addClass(Styles.Menubar.gridpane)
						style(append = true) {
							prefWidth = itemwidth
						}
						label(getLangString("help")) {
							addClass(Styles.Menubar.itemname)
							gridpaneConstraints {
								columnIndex = 0
							}
						}
						label {
							gridpaneConstraints {
								columnIndex = 1
								hGrow = Priority.ALWAYS
							}
							style(append = true) {
								prefWidth = 10.px
							}
						}
						label("Strg + H") {
							addClass(Styles.Menubar.itemshortcut)
							gridpaneConstraints {
								columnIndex = 2
								hGrow = Priority.SOMETIMES
							}
						}
					}
				}
				action {
					println("HELP")
				}
			}
		}
		center = tabpane {
			tab("Calender") {
				isClosable = false
				stackpane {
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

							}
							label("December") {
								addClass(Styles.CalendarView.title)
							}
							button(">") {

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

							isTableMenuButtonVisible = true
							columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY

							fun createGraphics(day: Day?): Node? {
								if(day == null)
									return null
								val graphicContainer = vbox {
									label(day.time.toString()) {
										style(append = true) {
											borderColor += box(Color.GOLD,Color.AQUA,Color.BROWN,Color.BLUE)
											borderRadius += box(4.px)
										}
									}
								}
								return graphicContainer
							}

							fun cellfactory(): Callback<TableColumn<Week, Day?>, TableCell<Week, Day?>> {
								return Callback<TableColumn<Week, Day?>, TableCell<Week, Day?>> {
									val cell: TableCell<Week, Day?> = TableCell()
									cell.itemProperty().addListener {_, _, day2 ->
										createGraphics(day2)?.let {
											cell.graphic = it
										}
									}
									cell.addClass(Styles.CalendarView.tablecell)
									cell.onMouseEntered = EventHandler {
										println(it)
									}
									return@Callback cell
								}
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
	}
}