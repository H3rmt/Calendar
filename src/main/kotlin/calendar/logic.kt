package calendar

import calendar.Timing.toUTCEpochMinute
import javafx.beans.property.*
import javafx.collections.*
import logic.LogType
import logic.getLangString
import logic.log
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.IsoFields


val now: LocalDateTime = Timing.getNowLocal()

var calendarDisplay: LocalDate = Timing.getNowLocal().toLocalDate()

val currentMonth: ObservableList<Week> = FXCollections.observableArrayList()
val currentMonthName: SimpleStringProperty = SimpleStringProperty()

/**
 * called by buttons in calendar tab
 * and on startup
 */
fun changeMonth(right: Boolean) {
	calendarDisplay = calendarDisplay.plusMonths(if(right) 1 else -1)
	
	currentMonthName.set(getLangString(calendarDisplay.month.name))
	if(calendarDisplay.year != now.year)
		currentMonthName.value += "  " + calendarDisplay.year
	log("changing Month to ${calendarDisplay.month.name}")
	
	val data = generateMonth(calendarDisplay)
	currentMonth.clear()
	currentMonth.addAll(data)
}

/**
 * called at initialization
 * and loads 3 months and week appointments
 */
fun loadCalendarData() {
	currentMonthName.set(getLangString(calendarDisplay.month.name))
	if(calendarDisplay.year != now.year)
		currentMonthName.value += "  " + calendarDisplay.year
	log("set Month to ${calendarDisplay.month.name}")
	
	val data = generateMonth(calendarDisplay)
	currentMonth.clear()
	currentMonth.addAll(data)
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
			
			day.notes = getNotes(time.toUTCEpochMinute()).toMutableList()
			day.appointments = getAppointments(time.toUTCEpochMinute(), time.plusDays(1).toUTCEpochMinute()).toMutableList()
			
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
		week.notes.addAll(getWeekNotes(startTime.toUTCEpochMinute(), startTime.plusWeeks(1).toUTCEpochMinute()))
		// week.time.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
		
		log("added week: $week", LogType.LOW)
		weeks.add(week)
	} while(time.month == _time.month && time.dayOfMonth > 1)
	
	return weeks
}
