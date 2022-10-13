package ui


import calendar.Type
import calendar.Types
import ui.TabManager.Secure
import ui.styles.GlobalStyles
import ui.styles.MenubarStyles
import ui.styles.NoteStyles
import ui.styles.OverviewStyles
import ui.styles.ReminderStyles
import ui.styles.TabStyles
import ui.styles.WeekStyles
import ui.tabs.createOverviewTab
import ui.tabs.createReminderTab
import javafx.beans.property.*
import javafx.beans.value.*
import javafx.event.*
import javafx.geometry.*
import javafx.scene.control.*
import javafx.scene.image.*
import javafx.scene.layout.*
import javafx.stage.*
import logic.Configs
import logic.Language
import logic.LogType
import logic.ObservableValueListeners.listen
import logic.getConfig
import logic.log
import logic.translate
import org.controlsfx.control.ToggleSwitch
import tornadofx.*
import java.awt.image.BufferedImage
import java.io.PrintWriter
import java.io.StringWriter
import javax.imageio.IIOException
import javax.imageio.ImageIO
import kotlin.random.Random
import kotlin.reflect.KFunction


//https://edvin.gitbooks.io/tornadofx-guide/content/part1/7_Layouts_and_Menus.html

/** initialise JavaFx window */
fun initFrame() {
	createLoading()
	log("created loading")

	// replace default error handler with custom one that adds extra info to logging
	// suppresses default window and shows custom dialog
	DefaultErrorHandler.filter = {
		val writer = StringWriter()
		writer.append("Exit <ErrorCode: Frame Exception> -> ")

		if(getConfig(Configs.PrintStacktrace)) it.error.printStackTrace(PrintWriter(writer))
		else writer.append(it.error.toString())
		log(writer, LogType.ERROR)

		// switch to true to use custom error  (do in Release)
		if(!getConfig<Boolean>(Configs.Debug)) {
			it.consume()
			errorMessage(it.error, writer.toString())
		}
	}

	log("launching Application", LogType.IMPORTANT)
	launch<Window>()
	//LauncherImpl.launchApplication(Window::class.java, PreloaderWindow::class.java, emptyArray())
}

/** custom error window */
fun errorMessage(error: Throwable, writer: String) = Alert(Alert.AlertType.ERROR).apply {
	val err: Throwable = error.cause ?: error
	headerText = err.message ?: "An error occurred"
	isResizable = true
	title = "Error in " + err.stackTrace[0].fileName
	dialogPane.content = VBox().apply {
		label(writer.substringBefore('\n').replace("->", "\n"))

		if(getConfig<Boolean>(Configs.Debug)) textarea {
			prefRowCount = 10
			text = writer
		}
	}
	showAndWait()
}

const val DEFAULTHEIGHT = 600.0
const val DEFAULTWIDTH = 800.0

class Window: App(
	MainView::class,
	GlobalStyles::class,
	MenubarStyles::class,
	TabStyles::class,
	NoteStyles::class,
	ReminderStyles::class,
	OverviewStyles::class,
	WeekStyles::class
) {
	override fun start(stage: Stage) {
		stage.height = DEFAULTHEIGHT
		stage.width = DEFAULTWIDTH
		super.start(stage)
		log("started Frame", LogType.IMPORTANT)

		// switch loading off
		removeLoading()

		// TODO open last tabs
		TabManager.openTab("reminders", ::createReminderTab)
		TabManager.openTab("calendar", ::createOverviewTab)
	}
}

class MainView: View("Calendar") {
	override val root = borderpane {
		top = createMenuBar(this)
		log("created menubar", LogType.IMPORTANT)
		center = tabpane {
			tabDragPolicy = TabPane.TabDragPolicy.REORDER
			tabClosingPolicy = TabPane.TabClosingPolicy.ALL_TABS
			TabManager.pane = this
		}
		log("created pane", LogType.IMPORTANT)
	}
}

/**
 * Manges tabs for the frame and is used to create, override and close tabs
 *
 * it contains another object with methods that must be handled with care
 *
 * @see Secure
 */
object TabManager {

	lateinit var pane: TabPane

	// list of all currently open tabs
	private val tabs: MutableMap<String, Tab> = mutableMapOf()

	/**
	 * creates a new tab, or focuses the tab if it already exists
	 *
	 * takes an identifier, a function to create the tab and arguments for that
	 * function as arguments
	 *
	 * adds the tab to the pane if new created and sets focus on that tab
	 *
	 * Examples:
	 * > > "DayNotes2020/6/12" > "Week2012/43" > "Settings"
	 *
	 * must take a pane as its first parameter and return ab Tab
	 *
	 * Examples:
	 * > > ::createWeekTab(fun createWeekTab(pane: TabPane, week: Week, day:
	 * > frame.Day?): Tab) > ::createNoteTab(fun createNoteTab(pane: TabPane,
	 * > cell: Celldisplay): Tab)
	 *
	 * Examples:
	 * > > week,day > data
	 *
	 * @param identifier this should be a unique identifier for the tab like
	 *     the title but with some extra information so that it doesn't prevent
	 *     another tab from getting created and instead joinks focus
	 * @param createFunction the function to create the tab
	 * @param methodArgs add all extra parameters apart from the pane here
	 */
	fun openTab(identifier: String, createFunction: KFunction<Tab>, vararg methodArgs: Any?) {
		log("tab $identifier opened")
		val newTab = tabs.getOrElse(identifier) {
			createFunction.call(pane, *methodArgs)
		}
		tabs[identifier] = newTab
		newTab.setOnClosed {
			log("tab $identifier closed")
			tabs.remove(identifier)
		}

		pane.selectionModel.select(newTab)
	}

	/**
	 * this part contains methods for the Tabmanager that should be handled
	 * carefully.
	 */
	object Secure {
		@Suppress("unused")
		fun closeTab(identifier: String) {
			log("tab $identifier closed")
			tabs[identifier]?.close()
			tabs.remove(identifier)
		}

		fun overrideTab(identifier: String, createFunction: KFunction<Tab>, vararg methodArgs: Any?) {
			log("tab $identifier overridden")
			closeTab(identifier)
			openTab(identifier, createFunction, methodArgs)
		}
	}

	override fun toString(): String = tabs.keys.toString()
}

// maps already loaded images to their path
val cache = mutableMapOf<String, Image>()

/**
 * Create fx image from path
 *
 * @param name name of the image
 * @param path path to the image
 */
fun createFXImage(name: String, path: String = ""): Image {
	val path = "img/$path/$name"
	// return cached result
	cache[path]?.let { return it }

	@Suppress("SwallowedException")
	val image = try {
		ImageIO.read({}::class.java.classLoader.getResource(path))
	} catch(e: IllegalArgumentException) {
		log("file not found:$path", LogType.WARNING)
		getImageMissing()
	} catch(e: IIOException) {
		log("can't read file:$path", LogType.WARNING)
		getImageMissing()
	}

	// convert to Image
	val wr = WritableImage(image.width, image.height)
	val pw = wr.pixelWriter
	for(x in 0 until image.width) {
		for(y in 0 until image.height) {
			pw.setArgb(x, y, image.getRGB(x, y))
		}
	}
	cache[path] = wr
	return wr
}

/** create BufferedImage with random colors to replace missing image */
fun getImageMissing(): BufferedImage {
	val im = BufferedImage(30, 30, BufferedImage.TYPE_3BYTE_BGR)
	val g2 = im.graphics
	for(i in 0 until 6)
		for(j in 0 until 10) {
			g2.color = java.awt.Color(Random.nextInt(255), Random.nextInt(155), Random.nextInt(155))
			g2.fillRect(1 + i * 5, 1 + j * 3, 5, 3)
		}

	g2.dispose()
	return im
}

/** extension function to create ToggleSwitch from FXcontrolls */
fun EventTarget.toggleSwitch(
	text: ObservableValue<String>? = null,
	selected: Property<Boolean> = true.toProperty(),
	op: ToggleSwitch.() -> Unit = {}
) = ToggleSwitch().attachTo(this, op) {
	it.selectedProperty().bindBidirectional(selected)
	if(text != null)
		it.textProperty().bind(text)
}

/**
 * property containing a string that gets translated if requested
 *
 * @param initialValue initialValue
 * @param type Type of Translation
 * @param args args to forward to format
 */
class TranslatingSimpleStringProperty(
	initialValue: String = "",
	private val type: Language.TranslationTypes,
	private vararg val args: Any
): SimpleStringProperty(initialValue) {
	override fun set(newValue: String?) {
		super.set(newValue)
	}

	override fun get(): String = super.get().takeIf { it != "" }?.translate(type, args) ?: "" // don't try to translate if empty

}

/**
 * extension function to create a Combobox with a Property for the selected
 * Type
 *
 * @param type Property to bind with the combobox
 */
fun EventTarget.typeCombobox(type: Property<Type>): ComboBox<Type> {
	return combobox(values = Types, property = type) {
		buttonCell = object: ListCell<Type>() {
			override fun updateItem(item: Type?, empty: Boolean) {
				super.updateItem(item, empty)
				if(empty || item == null) {
					text = null
					graphic = null
				} else {
					text = item.name.value
				}
			}
		}
		setCellFactory {
			return@setCellFactory object: ListCell<Type>() {
				override fun updateItem(item: Type?, empty: Boolean) {
					super.updateItem(item, empty)
					if(empty || item == null) {
						text = null
						graphic = null
					} else {
						text = item.name.value
					}
				}
			}
		}
	}
}

/**
 * adjusts width of the scrollbar Property to the width ot scrollbar
 *
 * @param scrollbarWidth Property to update if width updates
 */
fun ScrollPane.adjustWidth(scrollbarWidth: DoubleProperty) {
	widthProperty().listen(removeAfterRun = true) { ->
		lookupAll(".scroll-bar").filterIsInstance<ScrollBar>()
			.filter { it.orientation == Orientation.VERTICAL }[0].let { bar ->
			bar.visibleProperty().listen(runOnce = true) { visible ->
				if(visible) {
					scrollbarWidth.value = 13.3 + 2 // 13.3 scrollbar  2 padding right of inner vbox
				} else {
					scrollbarWidth.value = 2.0 // 2 padding right of inner vbox
				}
			}
		}
	}
}
