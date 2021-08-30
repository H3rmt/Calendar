import calendar.prepareAppointments
import calendar.preparedsingleAppointments
import calendar.preparedweeklyAppointments
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
	
	log("Updated Logger with config data\n", LogType.IMPORTANT)
	updateLogger()
	
	log("preparing Appointments", LogType.IMPORTANT)
	prepareAppointments()
	log("prepared week Appointments $preparedweeklyAppointments", LogType.NORMAL)
	log("prepared single Appointments $preparedsingleAppointments", LogType.NORMAL)
	
	log("starting Frame", LogType.IMPORTANT)
	frameInit()
	
	log("exiting Frame", LogType.IMPORTANT)
	exitProcess(1)
}