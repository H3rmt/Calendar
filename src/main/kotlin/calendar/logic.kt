package calendar

import logic.getJson as Json
import calendar.Timing.UTCEpochMinuteToLocalDateTime
import javafx.beans.property.*
import javafx.collections.*
import logic.ConfigFiles
import logic.LogType
import logic.emptyDefault
import logic.getJsonReader
import logic.getLangString
import logic.log
import java.io.File
import java.io.FileReader
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.time.ZoneOffset
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
	
	preparedDayNotes.remove(calendarDisplay.month.minus(if(right) 2 else -2))
	preparedWeekNotes.remove(calendarDisplay.month.minus(if(right) 2 else -2))
	preparedsingleAppointments.remove(calendarDisplay.month.minus(if(right) 2 else -2))
	
	prepareMonthAppointments(calendarDisplay.month.plus(if(right) 1 else -1), calendarDisplay.year)
	prepareMonthNotes(calendarDisplay.month.plus(if(right) 1 else -1), calendarDisplay.year)
	
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
	
	preparedweeklyAppointments.clear()
	preparedDayNotes.clear()
	preparedWeekNotes.clear()
	preparedsingleAppointments.clear()
	
	run {
		prepareWeekAppointments()
		prepareMonthAppointments(calendarDisplay.month.minus(1), calendarDisplay.year)
		prepareMonthNotes(calendarDisplay.month.minus(1), calendarDisplay.year)
		prepareMonthAppointments(calendarDisplay.month, calendarDisplay.year)
		prepareMonthNotes(calendarDisplay.month, calendarDisplay.year)
		prepareMonthAppointments(calendarDisplay.month.plus(1), calendarDisplay.year)
		prepareMonthNotes(calendarDisplay.month.plus(1), calendarDisplay.year)
	}
	
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
			day.appointments.addAll(preparedsingleAppointments[time.month]?.get(time.dayOfMonth) ?: listOf())
			day.notes.addAll(preparedDayNotes[time.month]?.get(time.dayOfMonth) ?: listOf())
			
			days.add(day)
			time = time.plusDays(1)
		} while(time.dayOfWeek.value != 1)
		
		val startTime = time.minusDays(7)
		
		val week = Week(
			startTime,
			days[0], days[1], days[2], days[3], days[4], days[5], days[6],
			startTime.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
		)
		week.addAppointments(preparedweeklyAppointments)
		week.notes.addAll(preparedWeekNotes[startTime.month]?.get(week.time.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)) ?: listOf())
		
		log("added week: $week", LogType.LOW)
		weeks.add(week)
	} while(time.month == time.month && time.dayOfMonth > 1)
	
	return weeks
}


// Day: { Appointments }
val preparedweeklyAppointments: MutableMap<DayOfWeek, MutableList<WeekAppointment>> = mutableMapOf()

// ----overridden with each Month change----

// Month: { Day of month: Appointments }
val preparedsingleAppointments: MutableMap<Month, MutableMap<Int, MutableList<Appointment>>> = mutableMapOf()

// Month: { Day of month: Note }
val preparedDayNotes: MutableMap<Month, MutableMap<Int, MutableList<Note>>> = mutableMapOf()

// Month: { Week of year: Note }
val preparedWeekNotes: MutableMap<Month, MutableMap<Int, MutableList<Note>>> = mutableMapOf()


fun prepareWeekAppointments() {
	File(ConfigFiles.weekAppointmentsFile).run {
		if(!exists()) {
			createNewFile()
			writeText(emptyDefault)
			log("created default weekAppointmentsFile:${ConfigFiles.weekAppointmentsFile}", LogType.WARNING)
		}
	}
	resetGroup(IDGroups.Appointments)
	Json().fromJson<ArrayList<Map<String, Any>>>(getJsonReader(FileReader(ConfigFiles.weekAppointmentsFile)), List::class.java).forEach { list ->
		log("reading Week Appointment: $list", LogType.LOW)
		WeekAppointment.fromJSON<WeekAppointment>(list)?.run {
			preparedweeklyAppointments.putIfAbsent(day, mutableListOf())
			
			usedID(IDGroups.Appointments, id)
			preparedweeklyAppointments[day]!!.add(this)
			log("loaded Week Appointment: $this", LogType.LOW)
		}
	}
	log("prepared Week Appointments $preparedweeklyAppointments", LogType.NORMAL)
}


/**
 * loads appointments for this
 * the leading and trailing month
 */
private fun prepareMonthAppointments(Month: Month, _time: Int) {
	File(ConfigFiles.appointmentsDir + "${_time}/$Month.json").run {
		if(!exists()) {
			log("file with appointments for ${_time}/$Month not found", LogType.LOW)
			return
		}
	}
	resetGroup(IDGroups.Appointments)
	Json().fromJson<ArrayList<Map<String, Any>>>(getJsonReader(FileReader(ConfigFiles.appointmentsDir + "/${_time}/$Month.json")), List::class.java).forEach { list ->
		log("reading single Appointment: $list", LogType.LOW)
		Appointment.fromJSON<Appointment>(list)?.run {
			val time = UTCEpochMinuteToLocalDateTime(start)
			val timeEnd = UTCEpochMinuteToLocalDateTime(start + duration)
			
			var tempTime = time
			do {
				preparedsingleAppointments.putIfAbsent(tempTime.month, mutableMapOf())
				preparedsingleAppointments[tempTime.month]!!.putIfAbsent(tempTime.dayOfMonth, mutableListOf())
				
				preparedsingleAppointments[tempTime.month]!![tempTime.dayOfMonth]!!.add(this)
				tempTime = tempTime.plusDays(1)
			} while(tempTime <= timeEnd)
			
			usedID(IDGroups.Appointments, id)
			log("loaded single Appointment: $this", LogType.LOW)
		}
	}
	log("prepared single Appointments $preparedsingleAppointments", LogType.NORMAL)
}


fun prepareMonthNotes(Month: Month, _time: Int) {
	File(ConfigFiles.notesDir + "/${_time}/$Month.json").run {
		if(!exists()) {
			log("file with notes for /${_time}/$Month not found", LogType.LOW)
			return
		}
	}
	resetGroup(IDGroups.Notes)
	Json().fromJson<Map<String, ArrayList<Map<String, Any>>>>(getJsonReader(FileReader(ConfigFiles.notesDir + "/${_time}/$Month.json")), Map::class.java)
		.forEach { (name, list) ->
			when(name) {
				"Day Notes" -> {
					log("reading Day Notes", LogType.LOW)
					list.forEach {
						Note.fromJSON<Note>(it)?.apply {
							val time = LocalDate.ofInstant(Instant.ofEpochSecond(time * 60), ZoneOffset.UTC)
							
							preparedDayNotes.putIfAbsent(time.month, mutableMapOf())
							preparedDayNotes[time.month]!!.putIfAbsent(time.dayOfMonth, mutableListOf())
							
							preparedDayNotes[time.month]!![time.dayOfMonth]!!.add(this)
							
							usedID(IDGroups.Notes, id)
							log("loaded Day Note: $this", LogType.LOW)
						}
					}
					log("prepared day Notes $preparedDayNotes", LogType.NORMAL)
				}
				"Week Notes" -> {
					log("reading Week Notes", LogType.LOW)
					list.forEach {
						Note.fromJSON<Note>(it)?.apply {
							val time = LocalDate.ofInstant(Instant.ofEpochSecond(time * 60), ZoneOffset.UTC)
							
							preparedWeekNotes.putIfAbsent(time.month, mutableMapOf())
							preparedWeekNotes[time.month]!!.putIfAbsent(time.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR), mutableListOf())
							
							preparedWeekNotes[time.month]!![time.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)]!!.add(this)
							
							usedID(IDGroups.Notes, id)
							log("loaded week Note: $this", LogType.LOW)
						}
					}
					log("prepared Week Notes $preparedWeekNotes", LogType.NORMAL)
				}
			}
		}
	
}
