package logic

import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList

// -------------------------- listeners --------------------------

// ---------------- ObservableValues ----------------

/**
 * Listen
 *
 * @param listener
 * @param runOnce
 * @param removeAfterRun
 * @param T
 * @receiver
 */
fun <T> ObservableValue<T>.listen(
	listener: (value: T) -> Unit, runOnce: Boolean = false, removeAfterRun: Boolean = false
) = listen(runOnce = runOnce, removeAfterRun = removeAfterRun, listener)


/**
 * Listen
 *
 * @param runOnce
 * @param removeAfterRun
 * @param listener
 * @param T
 * @receiver
 */
fun <T> ObservableValue<T>.listen(
	runOnce: Boolean = false, removeAfterRun: Boolean = false, listener: (value: T) -> Unit
) {
	lateinit var lst: ChangeListener<T>
	lst = ChangeListener<T> { _, _, newValue ->
		listener(newValue)
		if(removeAfterRun)
			this.removeListener(lst)

	}
	addListener(lst)
	if(runOnce)
		listener(this.value)
}

/**
 * Listen2
 *
 * @param runOnce
 * @param removeAfterRun
 * @param listener
 * @param T
 * @receiver
 */
fun <T> ObservableValue<T>.listen2(
	runOnce: Boolean = false, removeAfterRun: Boolean = false, listener: (value: T, old: T) -> Unit
) {
	lateinit var lst: ChangeListener<T>
	lst = ChangeListener<T> { _, oldValue, newValue ->
		listener(newValue, oldValue)
		if(removeAfterRun)
			this.removeListener(lst)
	}
	this.addListener(lst)
	if(runOnce)
		listener(this.value, this.value)
}

// ---------------- ObservableLists ----------------

/**
 * Listen updates
 *
 * @param listener
 * @param F
 * @param T
 * @receiver
 */
fun <F, T: ObservableList<F>> T.listenUpdates(
	listener: (new: ListChangeListener.Change<out F>) -> Unit
) {
	addListener(ListChangeListener { change ->
		listener(change)
	})
}

/**
 * Listen
 *
 * @param listener
 * @param runOnce
 * @param F
 * @param T
 * @receiver
 */
fun <F, T: ObservableList<F>> T.listen(
	listener: (new: List<F>) -> Unit, runOnce: Boolean = false
) = listen(runOnce, listener)

/**
 * Listen
 *
 * @param runOnce
 * @param listener
 * @param F
 * @param T
 * @receiver
 */
fun <F, T: ObservableList<F>> T.listen(
	runOnce: Boolean = false, listener: (new: List<F>) -> Unit
) {
	addListener(ListChangeListener { change ->
		listener(change.list)
	})
	if(runOnce)
		listener(this)
}

// -------------------------- logging --------------------------

/**
 * Log
 *
 * @param T
 * @return
 */
fun <T> T.log(): T {
	println(this)
	return this
}

/**
 * Log
 *
 * @param name
 * @param T
 * @return
 */
fun <T: ObservableValue<*>> T.log(name: String = ""): T {
	listen2 { old, new ->
		println("change ${name.ifEmpty { "ObservableValue" }} ($this): $old -> $new")
	}
	return this
}

/**
 * Log
 *
 * @param name
 * @param F
 * @param T
 * @return
 */
fun <F, T: ObservableList<F>> T.log(name: String = ""): T {
	listenUpdates { change ->
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

	}
	return this
}
