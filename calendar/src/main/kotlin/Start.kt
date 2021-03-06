import calendar.Appointments
import calendar.Files
import calendar.Notes
import calendar.Reminders
import calendar.Types
import calendar.initDb
import frame.frameInit
import javafx.beans.property.*
import logic.*
import kotlin.system.exitProcess

var DEV = false

fun main(args: Array<String>) {
	println("\nStarting Calendar... ${args.contentToString()} \n")
	if(args.contains("dev"))
		DEV = true

	initLogger()
	log("initialised Logger", LogType.IMPORTANT)

	init()

	log("starting Frame", LogType.IMPORTANT)
	frameInit()

	log("exiting Frame", LogType.IMPORTANT)
	exitProcess(0)
}

fun init() {
	initConfigs()
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

fun String.replaceNewline(): String = this.replace("\n", "\\n")

@Suppress("UNCHECKED_CAST")
fun <T, TNotNull> Property<T>.nullIfValueNull(): Property<TNotNull>? = if(this.value == null) null else this as Property<TNotNull>
