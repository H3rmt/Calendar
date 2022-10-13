package calendar

import calendar.Timing.toUTCEpochMinute
import com.sun.javafx.collections.ObservableListWrapper
import javafx.beans.property.*
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Interface for DBObservableBase
 *
 * @param T Type of Observable
 * @param DB Type stored in DB
 */
interface IDBObservableD<T, DB> {
	/** function to get the actual db... variable by Column in DBClass */
	fun abstractGet(): DB

	/** function to set the actual db... variable by Column in DBClass */
	fun abstractSet(dat: DB)

	/** function to convert a type from the Observable to Type for DB */
	fun convertFrom(value: T): DB

	/** function to convert a type from DB to the Observable Type */
	fun convertTo(value: DB): T
}

/**
 * Base Class for DBObservables
 *
 * @param T Type in DB
 * @param DB Type in Observable
 * @see IDBObservableD
 * @see ObjectPropertyBase
 * @see DBObservable
 * @see DBDateObservable
 * @see DBDateTimeObservable
 */
abstract class DBObservableBase<T, DB>: ObjectPropertyBase<T>(), IDBObservableD<T, DB> {
	// false if value was never loaded
	private var loaded = false
	override fun getBean(): Any = "--Bean--"

	override fun getName(): String = "DBObservableBase"

	private fun reload(hasTransaction: Boolean = false) {
		if(!hasTransaction)
			transaction {
				set(convertTo(abstractGet()))
			}
		else
			set(convertTo(abstractGet()))
		loaded = true
	}

	override fun get(): T {
		if(!loaded)
			reload()
		return super.get()
	}

	override fun set(newValue: T) {
		transaction {
			abstractSet(convertFrom(newValue))
		}
		super.set(newValue)
	}

	fun set(v: Property<T>) {
		set(v.value)
	}

	override fun hashCode(): Int = value.hashCode()

	/** used for equals methods of DB Classes */
	override fun equals(other: Any?): Boolean {
		if(this === other)
			return true
		if(other !is DBObservableBase<*, *>)
			return false
		if(value != other.value)
			return false

		return true
	}

	/** creates a Property with this.value */
	fun cloneProp(): Property<T> = SimpleObjectProperty(value)

	override fun toString(): String = "[DBO(${get()})]"
}


/**
 * class implementing DBObservableBase with DBType == ObservableType
 *
 * @param T Type in observable and DB
 * @see DBObservableBase
 */
abstract class DBObservable<T>: DBObservableBase<T, T>() {
	override fun convertFrom(value: T): T = value
	override fun convertTo(value: T): T = value
}

/**
 * class implementing DBObservableBase with DBType == Long and
 * ObservableType == LocalDate
 *
 * automatically implements conversion functions to convert LocalDate to
 * Long and other way around
 *
 * @see LocalDate
 * @see DBObservableBase
 */
abstract class DBDateObservable: DBObservableBase<LocalDate, Long>() {
	override fun convertFrom(value: LocalDate): Long = value.toUTCEpochMinute()
	override fun convertTo(value: Long): LocalDate = Timing.fromUTCEpochMinuteToLocalDateTime(value).toLocalDate()
}

/**
 * class implementing DBObservableBase with DBType == Long and
 * ObservableType == LocalDateTime
 *
 * automatically implements conversion functions to convert LocalDateTime
 * to Long and other way around
 *
 * @see LocalDateTime
 * @see DBObservableBase
 */
abstract class DBDateTimeObservable: DBObservableBase<LocalDateTime, Long>() {
	override fun convertFrom(value: LocalDateTime): Long = value.toUTCEpochMinute()
	override fun convertTo(value: Long): LocalDateTime = Timing.fromUTCEpochMinuteToLocalDateTime(value)
}

/**
 * List that wraps an ObservableList and can get subscribed to to get
 * updates if Items change
 *
 * @param T Type of Entity
 * @property table entity [Appointment], [Reminder], ... ]
 */
open class DBObservableList<T: Entity<*>>(private val table: EntityClass<*, T>):
	ObservableListWrapper<T>(mutableListOf()) {
	private var loaded = false

	fun reload() {
		transaction {
			super.setAll(table.all().toList())
		}
	}

	override fun toString(): String = "[DBObservableList value: ${super.toString()}]"
}
