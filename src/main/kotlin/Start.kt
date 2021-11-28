import calendar.loadCalendarData
import frame.frameInit
import javafx.beans.value.*
import logic.LogType
import logic.configs
import logic.initConfigs
import logic.initLogger
import logic.log
import logic.updateLogger
import kotlin.system.exitProcess

fun main() {
	println("\n\tStarting Calendar... \n")
	
	initLogger()
	log("initialised Logger", LogType.IMPORTANT)
	
	initConfigs()
	log("read Configs:$configs", LogType.IMPORTANT)
	
	log("Updating Logger with config data\n", LogType.IMPORTANT)
	updateLogger()
	log("Updated Logger", LogType.IMPORTANT)
	
	log("preparing Appointments", LogType.IMPORTANT)
	log("preparing Notes", LogType.IMPORTANT)
	loadCalendarData()
	
	log("starting Frame", LogType.IMPORTANT)
	frameInit()
	
	log("exiting Frame", LogType.IMPORTANT)
	exitProcess(1)
}

fun <T> T.lg(): T {
	println(this)
	return this
}



fun <T> ObservableValue<T>.lglisten(): ObservableValue<T> {
	this.addListener { ob, _, _ ->
		println(ob)
	}
	return this
}

fun <T> ObservableValue<T>.listen(listener: (new: T) -> Unit) {
	this.addListener { _, _, newValue ->
		listener(newValue)
	}
}

fun println(vararg any: Any) {
	any.forEach { print("$it ") }
	kotlin.io.println()
}
