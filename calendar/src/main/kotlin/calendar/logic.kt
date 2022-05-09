package calendar

import calendar.Timing.toUTCEpochMinute
import frame.Day
import frame.TranslatingSimpleStringProperty
import frame.Week
import javafx.collections.*
import javafx.collections.FXCollections.*
import logic.Language
import logic.LogType
import logic.log
import tornadofx.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.IsoFields


val now: LocalDateTime = Timing.getNowLocal()

val reminders: ObservableList<Reminder> = observableArrayList()
val appointments: ObservableList<Appointment> = observableArrayList()

var overviewTime: LocalDate = Timing.getNowLocal().toLocalDate()
val overviewWeeks: ObservableList<Week> = observableArrayList()
val overviewTitle: TranslatingSimpleStringProperty = TranslatingSimpleStringProperty(type = Language.TranslationTypes.Global)

/**
 * called by buttons in calendar tab
 * and on startup
 */
fun changeMonth(right: Boolean) {
	overviewTime = overviewTime.plusMonths(if(right) 1 else -1)
	loadCalendarData()
}


fun loadCalendarData() {
	overviewTitle.set(overviewTime.month.name)
	log("set Month to ${overviewTime.month.name}")
	
	val data = generateMonth(overviewTime)
	overviewWeeks.clear()
	overviewWeeks.addAll(data)
	
	reminders.clear()
	reminders.addAll(getReminders())
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
			
			day.notes = getNotes(time.toUTCEpochMinute()).toMutableList().toObservable()
//			log(time.dayOfWeek, LogType.IMPORTANT)
			day.appointments = getAppointments(time.toUTCEpochMinute(), time.plusDays(1).toUTCEpochMinute()).toMutableList()
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
		week.notes.addAll(getWeekNotes(startTime.toUTCEpochMinute(), time.toUTCEpochMinute()))
		log("added week: $week", LogType.LOW)
		weeks.add(week)
	} while(time.month == _time.month && time.dayOfMonth > 1)
	return weeks
}
