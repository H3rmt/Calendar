package frame


import javafx.scene.layout.Priority
import javafx.stage.Stage
import logic.getLangString
import tornadofx.App
import tornadofx.Dimension
import tornadofx.action
import tornadofx.addClass
import tornadofx.borderpane
import tornadofx.customitem
import tornadofx.gridpane
import tornadofx.gridpaneConstraints
import tornadofx.hgrow
import tornadofx.item
import tornadofx.label
import tornadofx.launch
import tornadofx.menu
import tornadofx.menubar
import tornadofx.px
import tornadofx.reloadStylesheetsOnFocus
import tornadofx.row
import tornadofx.separator


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
						addClass(Styles.menubaritem)
						gridpane {
							addClass(Styles.menubaritembox)
							row {
								label(getLangString("Show Reminder")) {
									addClass(Styles.menubaritemname)
									gridpaneConstraints {
										columnIndex = 2
										hgrow = Priority.SOMETIMES
									}
								}
								label("") {
									gridpaneConstraints {
										columnIndex = 1
										hgrow = Priority.ALWAYS
										minWidth = 10.0
										maxWidth = 200.0
									}
								}
								label("Strg + R") {
									addClass(Styles.menubaritemshortcut)
									gridpaneConstraints {
										columnIndex = 0
										hgrow = Priority.ALWAYS
									}
								}
							}
						}
					}
					customitem {
						addClass(Styles.menubaritem)
						gridpane {
							addClass(Styles.menubaritembox)
							row {
								label(getLangString("Show Calendar")) {
									addClass(Styles.menubaritemname)
									gridpaneConstraints {
										columnIndex = 0
										hgrow = Priority.SOMETIMES
									}
								}
								label {
									gridpaneConstraints {
										columnIndex = 1
										hgrow = Priority.ALWAYS
										minWidth = 10.0
										maxWidth = 200.0
									}
								}
								label("Strg + C") {
									addClass(Styles.menubaritemshortcut)
									gridpaneConstraints {
										columnIndex = 2
										hgrow = Priority.ALWAYS
									}
								}
							}
						}
					}
					customitem {
						addClass(Styles.menubaritem)
						gridpane {
							addClass(Styles.menubaritembox)
							row {
								label(getLangString("Show Some Long Name")) {
									addClass(Styles.menubaritemname)
									gridpaneConstraints {
										columnIndex = 0
										hgrow = Priority.NEVER
									}
								}
								label {
									gridpaneConstraints {
										columnIndex = 1
										hgrow = Priority.ALWAYS
										minWidth = 10.0
										maxWidth = 200.0
									}
								}
								label("Strg + N") {
									addClass(Styles.menubaritemshortcut)
									gridpaneConstraints {
										columnIndex = 2
										hgrow = Priority.ALWAYS
									}
								}
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

