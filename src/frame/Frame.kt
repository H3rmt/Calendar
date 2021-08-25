package frame


import calendar.setMonth
import javafx.application.*
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.stage.*
import logic.Exit
import logic.LogType
import logic.getLangString
import logic.log
import tornadofx.*


//https://edvin.gitbooks.io/tornadofx-guide/content/part1/7_Layouts_and_Menus.html

/**
 * BLOCKING
 */
fun frameInit() {
	createLoading()
	setMonth(true)
	try {
		Application.launch(Window::class.java)
	} catch(e: RuntimeException) {
	}
}

class Window: App(MainView::class, Styles::class) {
	
	override fun start(stage: Stage) {
		stage.height = 600.0
		stage.width = 700.0
		super.start(stage)
		removeLoading()
	}
}

class MainView: View("Calendar") {
	override val root = borderpane {
		try {
			top = createmenubar(this)
			log("created menubar", LogType.IMPORTANT)
			center = tabpane {
				createcalendartab(this@tabpane)
				log("created calendartab")
			}
			log("created tabpane", LogType.IMPORTANT)
		} catch(e: Exception) {
			log("Frame error", LogType.WARNING)
			throw Exit("444444",e)
		}
	}
}

fun createmenubar(pane: BorderPane): MenuBar {
	return pane.menubar {
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
}
