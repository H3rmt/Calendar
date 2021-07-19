package frame


import javafx.geometry.Pos
import javafx.scene.paint.Color
import javafx.scene.text.TextAlignment
import javafx.stage.Stage
import logic.getLangString
import tornadofx.App
import tornadofx.action
import tornadofx.addClass
import tornadofx.borderpane
import tornadofx.checkmenuitem
import tornadofx.contextmenu
import tornadofx.customitem
import tornadofx.hbox
import tornadofx.item
import tornadofx.label
import tornadofx.launch
import tornadofx.menu
import tornadofx.menubar
import tornadofx.reloadStylesheetsOnFocus
import tornadofx.separator
import tornadofx.style
import tornadofx.useMaxWidth
import tornadofx.vbox


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
						hbox(spacing = 15) {
							label(getLangString("Show Looooooooong Text")) {
								addClass(Styles.menubaritemname)
							}
							label(getLangString("Strg + T")) {
								addClass(Styles.menubaritemshortcut)
							}
						}
					}

					customitem {
						hbox {
							addClass(Styles.menubaritem)
							label(getLangString("Show Calendar")) {
								addClass(Styles.menubaritemname)
							}
							label(getLangString("Strg + C")) {
								addClass(Styles.menubaritemshortcut)
							}
						}
					}
					customitem {
						hbox(spacing = 15, alignment = Pos.CENTER_RIGHT) {
							label(getLangString("Show Reminder")) {
								addClass(Styles.menubaritemname)
							}
							label(getLangString("Strg + R")) {
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

