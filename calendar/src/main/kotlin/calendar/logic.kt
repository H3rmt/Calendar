package calendar

import calendar.Timing.toUTCEpochMinute
import com.sun.javafx.collections.ObservableListWrapper
import frame.TranslatingSimpleStringProperty
import javafx.beans.property.*
import javafx.collections.*
import lgListen
import logic.Language
import logic.LogType
import logic.log
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.IsoFields


val now: LocalDateTime = Timing.getNowLocal()

var calendarDisplay: LocalDate = Timing.getNowLocal().toLocalDate()

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
	// load everything
	Appointments.lgListen("Appointments").reload()
	Notes.lgListen("Notes").reload()
	Files.lgListen("Files").reload()
	Reminders.lgListen("Reminders").reload()
	Types.lgListen("Types").reload()
	
	
	// Overview TODO move this somewhere else
	currentMonthName.set(calendarDisplay.month.name)
	log("set Month to ${calendarDisplay.month.name}")
	currentMonth.setAll(generateMonth(calendarDisplay))
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
