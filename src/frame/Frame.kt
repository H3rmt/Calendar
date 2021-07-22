package frame


import javafx.scene.layout.Priority
import javafx.stage.Stage
import logic.getLangString
import tornadofx.App
import tornadofx.action
import tornadofx.addClass
import tornadofx.borderpane
import tornadofx.customitem
import tornadofx.gridpane
import tornadofx.gridpaneConstraints
import tornadofx.label
import tornadofx.launch
import tornadofx.menu
import tornadofx.menubar
import tornadofx.px
import tornadofx.separator
import tornadofx.style

//https://edvin.gitbooks.io/tornadofx-guide/content/part1/7_Layouts_and_Menus.html

class Application: App(MainView::class, Styles::class) {

	override fun start(stage: Stage) {
		stage.height = 300.0
		stage.width = 500.0
		super.start(stage)
	}
}

fun frameInit() {
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
		center = label(titleProperty) {

		}
	}
}

