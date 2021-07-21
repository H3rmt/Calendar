package frame


import javafx.geometry.Pos
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import javafx.stage.Stage
import logic.getLangString
import tornadofx.App
import tornadofx.action
import tornadofx.addClass
import tornadofx.borderpane
import tornadofx.constraintsForColumn
import tornadofx.customitem
import tornadofx.gridpane
import tornadofx.hbox
import tornadofx.item
import tornadofx.label
import tornadofx.launch
import tornadofx.menu
import tornadofx.menubar
import tornadofx.reloadStylesheetsOnFocus
import tornadofx.row
import tornadofx.separator
import tornadofx.vgrow


class Application: App(MainView::class, Styles::class) {
	init {
		reloadStylesheetsOnFocus()
	}

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
				item(getLangString("reload")) {
					action {
					}
				}
				separator()
				item(getLangString("quit")) {
					action {
					}
				}
			}
			menu(getLangString("view")) {
				menu(getLangString("show")) {
					customitem {
						hbox() {
							addClass(Styles.menubaritembox)
							label(getLangString("Show Looooooooong Text")) {
								addClass(Styles.menubaritemname)
							}
							label("Strg + T") {
								addClass(Styles.menubaritemshortcut)
							}
						}
					}
					customitem {
						addClass(Styles.menubaritem)
						gridpane {
							row {
								minHeight= 10.0
								vgrow= Priority.NEVER
							}
							alignment = Pos.CENTER
							maxWidth = 400.0
							prefWidth = 120.0
							label(getLangString("Show Calendar")) {
								addClass(Styles.menubaritemname)
							}
							label("Strg + C") {
								addClass(Styles.menubaritemshortcut)
							}
						}
					}
					customitem {
						hbox {
							addClass(Styles.menubaritembox)
							label(getLangString("Show Reminder")) {
								addClass(Styles.menubaritemname)
							}
							label("Strg + R") {
								addClass(Styles.menubaritemshortcut)
							}
						}
					}

				}
			}
			menu(getLangString("help")) {
				item(getLangString("reminder")) {
					action {
					}
				}
			}
		}
		center = label(titleProperty) {

		}
	}
}

