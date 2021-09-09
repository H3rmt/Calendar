package frame


import com.sun.javafx.application.LauncherImpl
import frame.Tabmanager.Secure
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
import kotlin.reflect.KFunction



//https://edvin.gitbooks.io/tornadofx-guide/content/part1/7_Layouts_and_Menus.html

/**
 * BLOCKING
 */
fun frameInit() {
	createLoading()
	log("created loading", LogType.NORMAL)
	
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
	
	log("launching Application", LogType.IMPORTANT)
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
		log("started Frame", LogType.NORMAL)
		Tabmanager.openTab("calendar", ::createcalendartab)
	}
}

class MainView: View("Calendar") {
	override val root = borderpane {
		top = createmenubar(this)
		log("created menubar", LogType.IMPORTANT)
		
		center = tabpane {
			tabDragPolicy = TabPane.TabDragPolicy.REORDER
			tabClosingPolicy = TabPane.TabClosingPolicy.ALL_TABS
			Tabmanager.tabpane = this@tabpane
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
				createmenuitem(this@menu, "Show Calendar", "Strg + Shift + C") {
					log("Show Calendar")
					Tabmanager.Secure.overrideTab("calendar", ::createcalendartab)
				}
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
	log("creating Menugroup with ${panes.size} elements", LogType.LOW)
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
	log("creating menuitem: $name $shortcut", LogType.LOW)
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

/**
 * Manges tabs for the frame and is used to
 * create, override and close tabs
 *
 * it contains another object with
 * methods that must be handled with care
 *
 * @see Secure
 */
object Tabmanager {
	
	lateinit var tabpane: TabPane
	
	/**
	 * <identifier, Tab>
	 */
	private val tabs: MutableMap<String, Tab> = mutableMapOf()
	
	/**
	 * creates a new tab, or focuses the tab if it already exists
	 *
	 * takes an identifier, a function to create the tab and arguments for that function
	 * as arguments
	 *
	 * adds the tab to the tabpane if new created and sets focus on that tab
	 *
	 * @param identifier this should be a unique identifier for the tab
	 *                   like the title but with some extra information
	 *                   so that it doesn't prevent another tab from getting
	 *                   created and instead joinks focus
	 *
	 *                   Examples:
	 *                   > "DayNotes2020/6/12"
	 *                   > "Week2012/43"
	 *                   > "Settings"
	 *
	 * @param createfunction the function to create the tab
	 *
	 *                       must take a tabpane as its first parameter
	 *                       and return ab Tab
	 *
	 *                       Examples:
	 *                       > ::createWeektab   (fun createweektab(pane: TabPane, week: Week, day: Day?): Tab)
	 *                       > ::createNoteTab   (fun createnotetab(pane: TabPane, cell: Celldisplay): Tab)
	 *
	 * @param methodargs add all extra parameters apart from the tabpane here
	 *
	 *                       Examples:
	 *                       > week,day
	 *                       > data
	 */
	fun openTab(identifier: String, createfunction: KFunction<Tab>, vararg methodargs: Any?) {
		val newtab = tabs.getOrElse(identifier) { createfunction.call(tabpane, *methodargs).also { tabs[identifier] = it } }
		
		tabpane.selectionModel.select(newtab)
	}
	
	/**
	 * this part contains methods
	 * for the Tabmanager that should
	 * be handled carefully.
	 */
	object Secure {
		
		fun closeTab(identifier: String) {
			tabs[identifier]?.close()
		}
		
		fun overrideTab(identifier: String, createfunction: KFunction<Tab>, vararg methodargs: Any?) {
			tabs[identifier]?.close()
			
			val newtab = createfunction.call(tabpane, *methodargs).also { tabs[identifier] = it }
			
			tabpane.selectionModel.select(newtab)
		}
	}
	
	override fun toString(): String = tabs.keys.toString()
	
}