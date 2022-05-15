import calendar.initDb
import calendar.loadCalendarData
import frame.frameInit
import javafx.beans.value.*
import javafx.collections.*
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
	
	log("preparing Data", LogType.IMPORTANT)
	loadCalendarData()
}

fun <T> T.lg(): T {
	println(this)
	return this
}

fun <T: ObservableValue<*>> T.lgListen(): T {
	println("lgListen on: $this ")
	listen2 { old, new ->
		println("lgListen ($this): $old -> $new")
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
	addListener(lst)
}

fun <F, T: ObservableList<F>> T.listen(listener: (new: ListChangeListener.Change<out F>) -> Unit) {
	addListener(ListChangeListener { change ->
		listener(change)
	})
}

fun <T: ObservableList<*>> T.lgListen(name: String = ""): T {
	addListener(ListChangeListener { change ->
		println("change ${name.ifEmpty { "ObservableValue" }} ($this): ")
		while(change.next()) {
			when(true) {
				change.wasAdded() -> {
					println("\tadded ${change.addedSubList} ")
				}
				change.wasRemoved() -> {
					println("\tremoved ${change.removed} ")
				}
				change.wasUpdated() -> {
					println("\tupdated ${change.list}")
				}
				else -> {}
			}
		}
	})
	return this
}

fun <T> ObservableValue<T>.listen2(once: Boolean = false, listener: (new: T, old: T) -> Unit) {
	lateinit var lst: ChangeListener<T>
	lst = ChangeListener<T> { _, oldValue, newValue ->
		listener(newValue, oldValue)
		if(once)
			this.removeListener(lst)
	}
	this.addListener(lst)
}

fun println(vararg any: Any) {
	any.forEach { print("$it ") }
	kotlin.io.println()
}
