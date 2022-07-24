import calendar.Appointments
import calendar.Files
import calendar.Notes
import calendar.Reminders
import calendar.Types
import calendar.initDb
import frame.initFrame
import javafx.beans.property.*
import logic.LogType
import logic.configs
import logic.initLogger
import logic.loadConfigs
import logic.log
import logic.updateLogger
import kotlin.system.exitProcess

/**
 * set via *commandline args*
 *
 * should be used in development
 * - switches config, db and logfiles into current folder
 * - forces file and console logging
 * - sets logging level lower
 *
 * @see logic.Files
 * @see logic.updateLogger
 */
var DEV = false

/**
 * Main function to start the Application
 * - initialises the Logger
 * - initialises Application
 * - starts the Window
 *
 * @param args commandline args
 * @see initLogger
 * @see init
 * @see initFrame
 */
fun main(args: Array<String>) {
	println("\nStarting Calendar... ${args.contentToString()} \n")
	DEV = args.contains("dev")

	initLogger()
	log("initialised Logger", LogType.IMPORTANT)

	init()

	log("starting Frame", LogType.IMPORTANT)
	initFrame()

	log("exiting Frame", LogType.IMPORTANT)
	exitProcess(0)
}

/**
 * Initialises application (called at start or by menu button `reload`
 * - loads Configs
 * - updates Logger
 * - initialises DB
 * - syncs inMemoryCache with dbData
 *
 * @see loadConfigs
 * @see updateLogger
 * @see initDb
 * @see calendar.DBObservableList.reload
 */
fun init() {
	loadConfigs()
	log("read Configs:$configs", LogType.IMPORTANT)

	log("Updating Logger with config data", LogType.IMPORTANT)
	updateLogger()
	log("Updated Logger", LogType.IMPORTANT)

	initDb()

	log("preparing Data", LogType.IMPORTANT)
	Types.log("Types").reload()
	Appointments.log("Appointments").reload()
	Notes.log("Notes").reload()
	Files.log("Files").reload()
	Reminders.log("Reminders").reload()
	log("loaded Data", LogType.IMPORTANT)
}

/**
 * Returns a new string with newlines escaped as \n
 *
 * should be used for logging to prevent linebreaks in log messages
 *
 * @return escaped String
 */
fun String.replaceNewline(): String = this.replace("\n", "\\n")

/**
 * Returns null if a properties value is null, else return this property
 * with new nonNullable Type argument (allows ?. / ?: like method chaining
 * for properties)
 *
 * deadline is Property<T?>, so if the value contained inside deadline is
 * null it returns null, jumping to the next ?:
 * ```
 * pr: Property<T> = deadline?.nullIfValueNull() ?: time?.toProperty()
 * ```
 *
 * used when Property's from DBObjects are used, but default values are
 * provided of they are null
 *
 * @param T nullable TypeParameter of Property
 * @param TNotNull same parameter, but nullable
 * @return Property or null (Property`<TNotNull`>?)
 * @see frame.popup.ReminderPopup
 */
@Suppress("UNCHECKED_CAST")
fun <T, TNotNull> Property<T>.nullIfValueNull(): Property<TNotNull>? = if(this.value == null) null else this as Property<TNotNull>
