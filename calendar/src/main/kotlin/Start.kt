import calendar.initDb
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
	
	log("preparing Appointments", LogType.IMPORTANT)
	log("preparing Notes", LogType.IMPORTANT)
	loadCalendarData()
}

fun <T> T.lg(): T {
	println(this)
	return this
}

fun <T> ObservableValue<T>.lgListen(): ObservableValue<T> {
	println("lgListen on: $this ")
	this.addListener { ob, _, _ ->
		println("lgListen:", ob)
	}
	return this
}

fun <T> ObservableValue<T>.listen(once: Boolean = false, listener: (new: T) -> Unit) {
	lateinit var lst: ChangeListener<T>
	lst = ChangeListener<T> { _, _, newValue ->
		listener(newValue)
		if(once)
			this.removeListener(lst)
	}
	this.addListener(lst)
}

fun println(vararg any: Any) {
	any.forEach { print("$it ") }
	kotlin.io.println()
}
