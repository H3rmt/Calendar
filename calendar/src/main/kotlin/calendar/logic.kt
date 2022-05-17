package calendar

import Day
import Week
import calendar.Timing.toUTCEpochMinute
import com.sun.javafx.collections.ObservableListWrapper
import javafx.beans.property.*
import lgListen
import logic.LogType
import logic.log
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.IsoFields


fun loadCalendarData() {
	// load everything
	Appointments.lgListen("Appointments").reload()
	Notes.lgListen("Notes").reload()
	Files.lgListen("Files").reload()
	Reminders.lgListen("Reminders").reload()
	Types.lgListen("Types").reload()
}

fun generateMonth(_time: LocalDate): MutableList<Week> {
	log("generating Month", LogType.NORMAL)
	var time: LocalDate = _time.withDayOfMonth(1)
	val month = time.month
	
	val dayOffset = time.dayOfWeek.value
	
	// set start to beging of week
	time = time.minusDays((dayOffset - 1).toLong())
	
	val weeks: MutableList<Week> = mutableListOf()
	
	do {
		val days: MutableList<Day> = mutableListOf()
		do {
			val day = Day(time, time.month == month)
			
			// both notes and appointments get updated automatically
			day.notes = Notes.getNotesAt(time).lgListen("notes")
			day.appointments = Appointments.getAppointmentsFromTo(time.atStartOfDay(), time.atStartOfDay().plusDays(1), time.atStartOfDay().dayOfWeek).lgListen("day appointments")
			
			days.add(day)
			time = time.plusDays(1)
		} while(time.dayOfWeek.value != 1)
		
		val startTime = time.minusDays(7)
		
		val week = Week(
			startTime, days,
			startTime.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR),
			Notes.getWeekNotesFromTo(startTime, startTime.plusWeeks(1)).lgListen("week notes")
		)
		
		log("added week: $week", LogType.LOW)
		weeks.add(week)
	} while(time.month == _time.month && time.dayOfMonth > 1)
	return weeks
}

abstract class DBObservable<T>: DBObservableD<T, T>() {
	override fun convertFrom(value: T): T = value
	
	override fun convertTo(value: T): T = value
	
}

abstract class DBDateObservable: DBObservableD<LocalDate, Long>() {
	override fun convertFrom(value: LocalDate): Long = value.toUTCEpochMinute()
	override fun convertTo(value: Long): LocalDate = Timing.fromUTCEpochMinuteToLocalDateTime(value).toLocalDate()
}

abstract class DBDateTimeObservable: DBObservableD<LocalDateTime, Long>() {
	override fun convertFrom(value: LocalDateTime): Long = value.toUTCEpochMinute()
	override fun convertTo(value: Long): LocalDateTime = Timing.fromUTCEpochMinuteToLocalDateTime(value)
}

abstract class DBObservableD<T, DB>: ObjectPropertyBase<T>() {
	private var loaded = false
	override fun getBean(): Any = ""//TODO("Not yet implemented")
	
	override fun getName(): String = ""//TODO("Not yet implemented")
	
	private fun reload() {
		transaction {
			set(convertTo(abstractGet()))
		}
		loaded = true
	}
	
	abstract fun abstractGet(): DB
	
	abstract fun abstractSet(dat: DB)
	
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
	
	abstract fun convertFrom(value: T): DB
	
	abstract fun convertTo(value: DB): T
	
	override fun hashCode(): Int = value.hashCode()
	
	/**
	 * used for equals methods of DB Classes
	 */
	override fun equals(other: Any?): Boolean {
		if(this === other)
			return true
		if(other !is DBObservableD<*, *>)
			return false
		if(value != other.value)
			return false
		
		return true
	}
	
	fun clone(): Property<T> = SimpleObjectProperty(value)
	
	override fun toString(): String = "[DBO(${get()})]"
}

/**
 * only get() is allowed to get its value, this list must always be bound to
 *
 * set and setValue are also blocked, only remove and add
 */
open class DBObservableList<T: Entity<*>>(private val table: EntityClass<*, T>): ObservableListWrapper<T>(mutableListOf()) {
	private var loaded = false
	
	fun reload() {
		return transaction {
			super.setAll(table.all().toList())
		}
	}
	
	override fun toString(): String = "[DBObservableList value: ${super.toString()}]"
}