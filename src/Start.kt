
import calendar.loadCalendarData
import frame.frameInit
import logic.LogType
import logic.configs
import logic.initCofigs
import logic.initLogger
import logic.log
import logic.updateLogger
import kotlin.system.exitProcess

fun main(args: Array<String>) {
	println("\n\tStarting Calendar... \n")
	
	initLogger()
	log("initialised Logger", LogType.IMPORTANT)
	
	initCofigs()
	log("read Configs:$configs", LogType.IMPORTANT)
	
	log("Updating Logger with config data\n", LogType.IMPORTANT)
	updateLogger()
	log("Updated Logger", LogType.IMPORTANT)
	
	log("preparing Appointments", LogType.IMPORTANT)
	// TODO
	log("preparing Notes", LogType.IMPORTANT)
	loadCalendarData()
	
	log("starting Frame", LogType.IMPORTANT)
	frameInit()
	
	log("exiting Frame", LogType.IMPORTANT)
	exitProcess(1)
}