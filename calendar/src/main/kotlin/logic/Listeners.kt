package logic

import DEV
import javafx.beans.value.*
import javafx.collections.*

object ObservableValueListeners {

	/**
	 * executes a function that takes no parameters if the Observable Value
	 * changes
	 *
	 * allows function to be executed instantly or only once
	 *
	 * @param listener pass a reference to a function ::function
	 * @param runOnce runs the function once after register default: false
	 * @param removeAfterRun removes the listener after first run
	 * @param T Type of value stored in ObservableValue
	 */
	fun <T> ObservableValue<T>.listen(
		listener: () -> Unit,
		runOnce: Boolean = false,
		removeAfterRun: Boolean = false
	) {
		lateinit var lst: ChangeListener<T>
		lst = ChangeListener<T> { _, _, _ ->
			listener()
			if(removeAfterRun)
				this.removeListener(lst)

		}
		addListener(lst)
		if(runOnce)
			listener()
	}

	/**
	 * executes a function that takes no parameters if the Observable Value
	 * changes
	 *
	 * allows function to be executed instantly or only once
	 *
	 * @param runOnce runs the function once after register with current value
	 *     *default: false*
	 * @param removeAfterRun removes the listener after first run *default:
	 *     false*
	 * @param listener pass a function in {} after listen()
	 * @param T Type of value stored in ObservableValue
	 */
	fun <T> ObservableValue<T>.listen(
		runOnce: Boolean = false,
		removeAfterRun: Boolean = false,
		listener: () -> Unit
	) {
		listen(listener, runOnce = runOnce, removeAfterRun = removeAfterRun)
	}

	/**
	 * executes a function that takes a parameter of type [T] with the new
	 * value if the Observable Value changes
	 *
	 * allows function to be executed instantly or only once
	 *
	 * @param listener pass a reference to a function ::function
	 * @param runOnce runs the function once after register default: false
	 * @param removeAfterRun removes the listener after first run
	 * @param T Type of value stored in ObservableValue
	 */
	fun <T> ObservableValue<T>.listen(
		listener: (value: T) -> Unit,
		runOnce: Boolean = false,
		removeAfterRun: Boolean = false
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
	 * executes a function that takes a parameter of type [T] with the new
	 * value if the Observable Value changes
	 *
	 * allows function to be executed instantly or only once
	 *
	 * @param runOnce runs the function once after register with current value
	 *     *default: false*
	 * @param removeAfterRun removes the listener after first run *default:
	 *     false*
	 * @param listener pass a function in {} after listen()
	 * @param T Type of value stored in ObservableValue
	 */
	fun <T> ObservableValue<T>.listen(
		runOnce: Boolean = false,
		removeAfterRun: Boolean = false,
		listener: (value: T) -> Unit
	) {
		listen(listener, runOnce = runOnce, removeAfterRun = removeAfterRun)
	}

	/**
	 * executes a function that takes 2 parameter of type [T] with the new
	 * value and the old value if the Observable Value changes
	 *
	 * allows function to be executed instantly or only once
	 *
	 * @param listener pass a reference to a function ::function
	 * @param removeAfterRun removes the listener after first run *default:
	 *     false*
	 * @param runOnce runs the function once after register with current value
	 *     and current value *default: false*
	 * @param T Type of value stored in ObservableValue
	 */
	fun <T> ObservableValue<T>.listen(
		listener: (value: T, old: T) -> Unit,
		removeAfterRun: Boolean = false,
		runOnce: Boolean = false,
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

	/**
	 * executes a function that takes 2 parameter of type [T] with the new
	 * value and the old value if the Observable Value changes
	 *
	 * allows function to be executed instantly or only once
	 *
	 * @param runOnce runs the function once after register with current value
	 *     and current Value *default: false*
	 * @param removeAfterRun removes the listener after first run *default:
	 *     false*
	 * @param listener pass a function in {} after listen()
	 * @param T Type of value stored in ObservableValue
	 */
	fun <T> ObservableValue<T>.listen(
		runOnce: Boolean = false,
		removeAfterRun: Boolean = false,
		listener: (value: T, old: T) -> Unit
	) {
		listen(listener, removeAfterRun = removeAfterRun, runOnce = runOnce)
	}

	/**
	 * Logs changes to the console if this value changes (if [DEV] is true)
	 *
	 * @param name optional parameter to add a name to each log
	 * @param T Type of value stored in ObservableValue
	 * @return itself, so this method can be stacked
	 */
	fun <T: ObservableValue<*>> T.listenLog(name: String = ""): T {
		if(DEV)
			listen { old, new ->
				println("change ${name.ifEmpty { "ObservableValue" }} ($this): $old -> $new")
			}
		return this
	}
}

object ObservableListListeners {
	/**
	 * executes a function that takes 1 parameter of type
	 * [ListChangeListener.Change]<[T]> if the Observable List changes
	 *
	 * @param listener pass a function in {} after listen()
	 * @param F Type of ObservableList
	 * @param T Type of values stored in ObservableList
	 * @see ListChangeListener.Change
	 */
	fun <T, F: ObservableList<T>> F.listenChanges(
		listener: (new: ListChangeListener.Change<out T>) -> Unit
	) {
		addListener { change: ListChangeListener.Change<out T> ->
			listener(change)
		}
	}

	/**
	 * executes a function that takes 1 parameter of type [List]<[T]> if the
	 * Observable List changes
	 *
	 * @param listener pass a reference to a function ::function
	 * @param runOnce runs the function once after register with current value
	 *     *default: false*
	 * @param F Type of ObservableList
	 * @param T Type of values stored in ObservableList
	 */
	fun <T, F: ObservableList<T>> F.listen(
		listener: (new: List<T>) -> Unit, runOnce: Boolean = false
	) {
		addListener(ListChangeListener { change ->
			listener(change.list)
		})
		if(runOnce)
			listener(this)
	}

	/**
	 * executes a function that takes 1 parameter of type [List]<[T]> if the
	 * Observable List changes
	 *
	 * @param runOnce runs the function once after register with current value
	 *     *default: false*
	 * @param listener pass a function in {} after listen()
	 * @param F Type of ObservableList
	 * @param T Type of values stored in ObservableList
	 */
	fun <T, F: ObservableList<T>> F.listen(
		runOnce: Boolean = false, listener: (new: List<T>) -> Unit
	) {
		listen(listener, runOnce = runOnce)
	}

	/**
	 * Logs changes made to the ObservableList to the console (if [DEV] is
	 * true)
	 *
	 * @param name optional parameter to add a name to each log
	 * @param F Type of ObservableList
	 * @param T Type of values stored in ObservableList
	 * @return itself, so this method can be stacked
	 */
	fun <T, F: ObservableList<T>> F.listenLog(name: String = ""): F {
		if(DEV)
			listenChanges { change ->
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
}

