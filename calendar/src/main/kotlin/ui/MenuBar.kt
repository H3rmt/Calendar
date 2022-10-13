package ui

import calendar.Timing
import ui.popup.AppointmentPopup
import ui.popup.ReminderPopup
import ui.styles.MenubarStyles
import ui.tabs.createOverviewTab
import ui.tabs.createReminderTab
import init
import javafx.application.*
import javafx.scene.control.*
import javafx.scene.layout.*
import logic.Language
import logic.LogType
import logic.ObservableValueListeners.listen
import logic.log
import logic.translate
import tornadofx.*
import java.awt.Desktop
import java.io.IOException
import java.net.URI


/**
 * creates Menu Bar for window
 *
 * @param pane ref to parent
 */
fun createMenuBar(pane: BorderPane): MenuBar {
	return pane.menubar {
		menu("create".translate(Language.TranslationTypes.Menubar)) {
			createMenuGroup(createMenuItem(this@menu, "Appointment", "Strg + N") {
				AppointmentPopup.openNew(
					Timing.getNow(),
					Timing.getNow().plusHours(1),
					false
				)
			}, createMenuItem(this@menu, "Reminder", "Strg + R") {
				ReminderPopup.openNew(Timing.getNow(), null)
			})
		}
		menu("options".translate(Language.TranslationTypes.Menubar)) {
			createMenuGroup(createMenuItem(this@menu, "Reload", "F5") {
				log("reload triggered")
				init()
			}, createMenuItem(this@menu, "Preferences", "Strg + ,") {
				log("Preferences")
			}, run { separator(); return@run null }, createMenuItem(this@menu, "Quit", "Strg + Q") {
				log("exiting Program via quit")
				Platform.exit()
			})
		}
		menu("view".translate(Language.TranslationTypes.Menubar)) {
			createMenuGroup(createMenuItem(this@menu, "Show Reminder", "Strg + Shift + R") {
				log("Show Reminder")
				TabManager.Secure.overrideTab("reminders", ::createReminderTab)
			}, createMenuItem(this@menu, "Show Calendar", "Strg + Shift + C") {
				log("Show Calendar")
				TabManager.Secure.overrideTab("calendar", ::createOverviewTab)
			})
		}
		menu("help".translate(Language.TranslationTypes.Menubar)) {
			createMenuGroup(createMenuItem(this@menu, "Github", "") {
				log("Open Github")
				try {
					runAsync {
						Desktop.getDesktop().browse(URI("https://github.com/Buldugmaster99/Calendar"))
					}
				} catch(e: IOException) {
					log("failed to open browser $e", LogType.WARNING)
				}
			}, createMenuItem(this@menu, "Memory Usage", "") {
				//System.gc()
				val rt = Runtime.getRuntime()
				val usedMB = (rt.totalMemory() - rt.freeMemory()) / 1024 / 1024
				log("used memory: $usedMB  | max memory: ${rt.maxMemory() / 1024 / 1024}  | total memory ${rt.totalMemory() / 1024 / 1024}  | free memory ${rt.freeMemory() / 1024 / 1024}")
			}, run {
				separator(); return@run null
			}, createMenuItem(this@menu, "Help", "") {
				log("Help")
			})
		}
	}
}

/**
 * creates a Group of
 *
 * @param panes items of this group
 */
fun createMenuGroup(vararg panes: GridPane?) {
	var currentWidth = 10.0
	val changed = mutableListOf<GridPane>()
	val items = panes.filterNotNull()

	// make every item equal width
	items.forEach { item ->
		item.apply {
			widthProperty().listen { width ->
				if(!changed.contains(this))
					changed.add(this)
				if(width.toDouble() > currentWidth)
					currentWidth = width.toDouble()
				if(changed.size == items.size) items.forEach {
					it.prefWidth = currentWidth
				}
			}
		}
	}
}

/**
 * Creates menu item
 *
 * @param menu ref to parent
 * @param name name displayed on item
 * @param shortcut shortcut displayed for this action
 * @param action action to execute on click
 */
fun createMenuItem(menu: Menu, name: String, shortcut: String, action: () -> Unit): GridPane {
	lateinit var grid: GridPane
	menu.customitem {
		grid = gridpane {
			addClass(MenubarStyles.gridPane_)

			label(name.translate(Language.TranslationTypes.Menubar)) {
				addClass(MenubarStyles.itemName_)
				gridpaneConstraints {
					columnRowIndex(0, 0)
				}
			}
			label {
				addClass(MenubarStyles.spacing_)
				gridpaneConstraints {
					columnRowIndex(1, 0)
					hGrow = Priority.ALWAYS
				}
			}
			label(shortcut) {
				addClass(MenubarStyles.shortcut_)
				gridpaneConstraints {
					columnRowIndex(2, 0)
				}
			}
		}
		action(action)
	}
	return grid
}
