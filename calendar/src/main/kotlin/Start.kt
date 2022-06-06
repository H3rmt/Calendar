import calendar.*
import frame.frameInit
import logic.*
import kotlin.system.exitProcess

fun main() {
	println("\nStarting Calendar... \n")

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

fun String.replaceNewline(): String {
	return this.replace("\n", "\\n")
}