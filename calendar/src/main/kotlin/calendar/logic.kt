package calendar

import calendar.Timing.toUTCEpochMinute
import frame.TranslatingSimpleStringProperty
import javafx.beans.property.*
import javafx.collections.*
import logic.Language
import logic.LogType
import logic.log
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.IsoFields


val now: LocalDateTime = Timing.getNowLocal()

var calendarDisplay: LocalDate = Timing.getNowLocal().toLocalDate()

var reminders: ObservableList<Reminder> = FXCollections.observableArrayList()
val currentMonth: ObservableList<Week> = FXCollections.observableArrayList()

val currentMonthName: TranslatingSimpleStringProperty = TranslatingSimpleStringProperty(type = Language.TranslationTypes.Global)

/**
 * called by buttons in calendar tab
 * and on startup
 */
fun changeMonth(right: Boolean) {
	calendarDisplay = calendarDisplay.plusMonths(if(right) 1 else -1)
	loadCalendarData()
}

/**
 * called at initialization
 * and loads 3 months and week appointments
 */
fun loadCalendarData() {
	currentMonthName.set(calendarDisplay.month.name)
	log("set Month to ${calendarDisplay.month.name}")
	
	val data = generateMonth(calendarDisplay)
	currentMonth.clear()
	currentMonth.addAll(data)
	
	reminders.clear()
	reminders.addAll(getReminders())
}

fun generateMonth(_time: LocalDate): MutableList<Week> {
	log("generating Month", LogType.NORMAL)
	var time: LocalDate = _time.withDayOfMonth(1)
	val month = time.month
	
	val dayOffset = time.dayOfWeek.value
	time = time.minusDays((dayOffset - 1).toLong())
	
	val weeks: MutableList<Week> = mutableListOf()
	
	do {
		val days: MutableList<Day> = mutableListOf()
		do {
			val day = Day(time, time.month == month)
			
			day.notes = getNotes(time).toMutableList()
//			log(time.dayOfWeek, LogType.IMPORTANT)
			day.appointments = getAppointments(time.atStartOfDay(), time.atStartOfDay().plusDays(1)).toMutableList()
//			log(day.appointments, LogType.WARNING)
			days.add(day)
			time = time.plusDays(1)
		} while(time.dayOfWeek.value != 1)
		
		val startTime = time.minusDays(7)
		
		val week = Week(
			startTime,
			days[0], days[1], days[2], days[3], days[4], days[5], days[6],
			startTime.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
		)
		week.addAppointments(getWeekAppointments().toMutableList())
		week.notes.addAll(getWeekNotes(startTime, startTime.plusWeeks(1)))
		// week.time.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
		
		log("added week: $week", LogType.LOW)
		weeks.add(week)
	} while(time.month == _time.month && time.dayOfMonth > 1)
	return weeks
}

class DBObservable<T>(col: T?): DBObservableD<T, T>(col) {
	override fun convertFrom(value: T?): T? = value
	
	override fun convertTo(value: T?): T? = value
	
}

class DBDateObservable(col: Long?): DBObservableD<LocalDate, Long>(col) {
	override fun convertFrom(value: LocalDate?): Long? = value?.toUTCEpochMinute()
	override fun convertTo(value: Long?): LocalDate? = if(value != null) Timing.fromUTCEpochMinuteToLocalDateTime(value).toLocalDate() else null
}

class DBDateTimeObservable(col: Long?): DBObservableD<LocalDateTime, Long>(col) {
	override fun convertFrom(value: LocalDateTime?): Long? = value?.toUTCEpochMinute()
	override fun convertTo(value: Long?): LocalDateTime? = if(value != null) Timing.fromUTCEpochMinuteToLocalDateTime(value) else null
}

abstract class DBObservableD<T, DB>(private var col: DB?): ObjectPropertyBase<T>() {
	override fun getBean(): Any = TODO("Not yet implemented")
	
	override fun getName(): String = TODO("Not yet implemented")
	
	fun reload() {
		transaction {
			set(convertTo(col))
		}
	}
	
	override fun set(newValue: T?) {
		transaction {
			col = convertFrom(newValue)
		}
		super.set(newValue)
	}
	
	abstract fun convertFrom(value: T?): DB?
	
	abstract fun convertTo(value: DB?): T?
	
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
}
