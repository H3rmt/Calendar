import calendar.*
import frame.frameInit
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
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
	Types.lgListen("Types").reload()
	Appointments.lgListen("Appointments").reload()
	Notes.lgListen("Notes").reload()
	Files.lgListen("Files").reload()
	Reminders.lgListen("Reminders").reload()
	log("loaded Data", LogType.IMPORTANT)
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

/**
 * this one is used when listener is passed as a function pointer, other function would require to pass false
 * as once all the time
 */
fun <T> ObservableValue<T>.listen(
	listener: (new: T) -> Unit, runOnceWithValue: Boolean = false, once: Boolean = false
) = listen(once, runOnceWithValue, listener)

fun <T> ObservableValue<T>.listen(
	once: Boolean = false, runOnceWithValue: Boolean = false, listener: (new: T) -> Unit
) {
	lateinit var lst: ChangeListener<T>
	lst = ChangeListener<T> { _, _, newValue ->
		listener(newValue)
		if(once) this.removeListener(lst)
		
	}
	addListener(lst)
	if(runOnceWithValue) listener(value)
}

fun <F, T: ObservableList<F>> T.listen(listener: (new: ListChangeListener.Change<out F>) -> Unit) {
	addListener(ListChangeListener { change ->
		listener(change)
	})
}

fun <F, T: ObservableList<F>> T.listenAndRunOnce(listener: (new: List<F>) -> Unit) {
	addListener(ListChangeListener { change ->
		listener(change.list)
	})
	listener(this)
}

fun <T: ObservableList<*>> T.lgListen(name: String = ""): T {
	addListener(ListChangeListener { change ->
		println("change ${name.ifEmpty { "ObservableValue" }} [$size] ($this): ")
		while(change.next()) {
			if(change.wasAdded()) {
				println("\tadded:")
				change.addedSubList.forEach { println("\t\t$it") }
			}
			if(change.wasRemoved()) {
				println("\tremoved:")
				change.removed.forEach { println("\t\t$it") }
			}
			if(change.wasUpdated()) {
				println("\tupdated:")
				change.list.forEach { println("\t\t$it") }
			}
		}
		
	})
	return this
}

fun <T> ObservableValue<T>.listen2(once: Boolean = false, listener: (new: T, old: T) -> Unit) {
	lateinit var lst: ChangeListener<T>
	lst = ChangeListener<T> { _, oldValue, newValue ->
		listener(newValue, oldValue)
		if(once) this.removeListener(lst)
	}
	this.addListener(lst)
}

fun println(vararg any: Any) {
	any.forEach { print("$it ") }
	kotlin.io.println()
}

fun String.replaceNewline(): String {
	return this.replace("\n", "\\n")
}