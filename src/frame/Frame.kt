package frame


import calendar.setMonth
import com.sun.javafx.application.LauncherImpl
import javafx.application.*
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.stage.*
import logic.Configs
import logic.Exit
import logic.LogType
import logic.getConfig
import logic.getLangString
import logic.log
import tornadofx.*
import java.awt.Desktop
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter
import java.net.URI



//https://edvin.gitbooks.io/tornadofx-guide/content/part1/7_Layouts_and_Menus.html

/**
 * BLOCKING
 */
fun frameInit() {
	createLoading()
	setMonth(true)
	
	/**
	 * this looks pretty weird, but it essentially
	 * creates a stacktrace with the head of an Exit
	 * and the StackTrace of the actual exception
	 *
	 * /*
	 * if it is not an Exit(custom Exception) then
	 * Exit<errorocode> -> Exception is added at the beginning
	 * */
	 *
	 * @see Exit
	 */
	DefaultErrorHandler.filter = {
		val writer = StringWriter()
		
		//if(it.error::class != Exit::class) {
		writer.append("Exit <ErrorCode: Frame Exception> -> ")
		//}
		
		if(getConfig(Configs.Printstacktrace))
			it.error.printStackTrace(PrintWriter(writer))
		else
			writer.append(it.error.toString())
		log(writer, LogType.ERROR)
		
		// uncomment if errorpopup should be disabled TODO(Release)
		it.consume()
	}
	
	LauncherImpl.launchApplication(Window::class.java, PreloaderWindow::class.java, emptyArray())
}

class PreloaderWindow: Preloader() {
	override fun start(primaryStage: Stage) {
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
		top = createmenubar(this)
		log("created menubar", LogType.IMPORTANT)
		center = tabpane {
			tabClosingPolicy = TabPane.TabClosingPolicy.ALL_TABS
			createcalendartab(this@tabpane)
			log("created calendartab")
		}
		log("created tabpane", LogType.IMPORTANT)
	}
}

fun createmenubar(pane: BorderPane): MenuBar {
	return pane.menubar {
		menu(getLangString("options")) {
			createMenugroup(
				createmenuitem(this@menu, "Reload", "F5") { log("Reload") },
				createmenuitem(this@menu, "Preferences", "Strg + ,") { log("Preferences") },
				run { separator(); return@run null },
				createmenuitem(this@menu, "Quit", "Strg + Q") {
					log("exiting Program via quit", LogType.IMPORTANT)
					Platform.exit()
				}
			)
		}
		menu(getLangString("show")) {
			createMenugroup(
				createmenuitem(this@menu, "Show Reminder", "Strg + Shift + R") { log("Show Reminder") },
				createmenuitem(this@menu, "Show Calendar", "Strg + Shift + C") { log("Show Calendar") }
			)
		}
		menu(getLangString("help")) {
			createMenugroup(
				createmenuitem(this@menu, "Github", "") {
					log("Open Github", LogType.IMPORTANT)
					try {
						Desktop.getDesktop().browse(URI("https://github.com/Buldugmaster99/Calendar"))
					} catch(e: IOException) {
						log("failed to open browser", LogType.WARNING)
					}
				},
				run { separator(); return@run null },
				createmenuitem(this@menu, "Help", "") { log("Help") }
			)
		}
	}
}

fun createMenugroup(vararg panes: GridPane?) {
	var maxWidth = 10.0
	val items = panes.filterNotNull()
	val changed = mutableListOf<GridPane>()
	items.forEach { item ->
		item.apply {
			widthProperty().addListener(ChangeListener { _, _, newwidth ->
				if(!changed.contains(this))
					changed.add(this)
				if(newwidth.toDouble() > maxWidth)
					maxWidth = newwidth.toDouble()
				if(changed.size == items.size)
					items.forEach {
						it.prefWidth = maxWidth
					}
			})
		}
	}
}

fun createmenuitem(menu: Menu, name: String, shortcut: String, action: () -> Unit): GridPane? {
	var grid: GridPane? = null
	menu.customitem {
		grid = gridpane {
			addClass(Styles.Menubar.gridpane)
			
			label(getLangString(name)) {
				addClass(Styles.Menubar.itemname)
				gridpaneConstraints {
					columnRowIndex(0, 0)
				}
			}
			label {
				gridpaneConstraints {
					columnRowIndex(1, 0)
					hGrow = Priority.ALWAYS
					style {
						minWidth = 15.px
					}
				}
			}
			label(shortcut) {
				addClass(Styles.Menubar.itemshortcut)
				gridpaneConstraints {
					columnRowIndex(2, 0)
				}
			}
		}
		action(action)
	}
	
	return grid
}