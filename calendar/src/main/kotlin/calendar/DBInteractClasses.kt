package calendar

import calendar.Timing.toUTCEpochMinute
import com.sun.javafx.collections.ObservableListWrapper
import javafx.beans.property.ObjectPropertyBase
import javafx.beans.property.Property
import javafx.beans.property.SimpleObjectProperty
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate
import java.time.LocalDateTime

interface IDBObservableD<T, DB> {
	fun abstractGet(): DB
	fun abstractSet(dat: DB)
	fun convertFrom(value: T): DB
	fun convertTo(value: DB): T
}

abstract class DBObservableBase<T, DB>: ObjectPropertyBase<T>(), IDBObservableD<T, DB> {
	private var loaded = false
	override fun getBean(): Any = "--Bean--"//TODO("Not yet implemented")
	
	override fun getName(): String = "DBObservableBase"//TODO("Not yet implemented")
	
	private fun reload() {
		transaction {
			set(convertTo(abstractGet()))
		}
	}
	
	override fun get(): T {
		if(!loaded) reload()
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
	
	/**
	 * used for equals methods of DB Classes
	 */
	override fun equals(other: Any?): Boolean {
		if(this === other) return true
		if(other !is DBObservableBase<*, *>) return false
		if(value != other.value) return false
		
		return true
	}
	
	fun clone(): Property<T> = SimpleObjectProperty(value)
	
	override fun toString(): String = "[DBO(${get()})]"
}


abstract class DBObservable<T>: DBObservableBase<T, T>() {
	override fun convertFrom(value: T): T = value
	
	override fun convertTo(value: T): T = value
	
}

abstract class DBDateObservable: DBObservableBase<LocalDate, Long>() {
	override fun convertFrom(value: LocalDate): Long = value.toUTCEpochMinute()
	override fun convertTo(value: Long): LocalDate = Timing.fromUTCEpochMinuteToLocalDateTime(value).toLocalDate()
}

abstract class DBDateTimeObservable: DBObservableBase<LocalDateTime, Long>() {
	override fun convertFrom(value: LocalDateTime): Long = value.toUTCEpochMinute()
	override fun convertTo(value: Long): LocalDateTime = Timing.fromUTCEpochMinuteToLocalDateTime(value)
}

/**
 * only get() is allowed to get its value, this list must always be bound to
 *
 * set and setValue are also blocked, only remove and add
 */
open class DBObservableList<T: Entity<*>>(private val table: EntityClass<*, T>):
	ObservableListWrapper<T>(mutableListOf()) {
	private var loaded = false
	
	fun reload() {
		return transaction {
			super.setAll(table.all().toList())
		}
	}
	
	override fun toString(): String = "[DBObservableList value: ${super.toString()}]"
}
