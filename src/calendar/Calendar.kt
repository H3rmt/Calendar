package calendar

import logic.getJson as Json
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
import java.time.Month
import java.time.ZoneId.systemDefault
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.temporal.IsoFields


val now: ZonedDateTime = ZonedDateTime.now(systemDefault())

var calendarDisplay: ZonedDateTime = ZonedDateTime.now(systemDefault())

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
	
	prepareMonthAppointments(calendarDisplay.month.plus(if(right) 1 else -1))
	prepareMonthNotes(calendarDisplay.month.plus(if(right) 1 else -1))
	
	val data = generateMonth(calendarDisplay)
	currentMonth.clear()
	currentMonth.addAll(data)
}

fun saveDayNote(note: Note) {
	val noteTime = LocalDate.ofInstant(Instant.ofEpochSecond(note.time * 60), systemDefault())
	File(ConfigFiles.notesDir + "/${noteTime.month.name}.json").run {
		if(!exists()) {
			log("file with notes for ${noteTime.month.name} not found", LogType.LOW)
			createNewFile()
			writeText("{\n\t\"Week Notes\": [],\n\t\"Day Notes\": []\n}")
		}
	}
	
	val tmpDayNotes: MutableMap<Int, MutableList<Note>> = mutableMapOf()
	
	Json().fromJson<Map<String, ArrayList<Map<String, Any>>>>(getJsonReader(FileReader(ConfigFiles.notesDir + "/${noteTime.month.name}.json")), Map::class.java)
		.forEach { (name, list) ->
			when(name) {
				"Day Notes" -> {
					log("reading Day Notes", LogType.LOW)
					list.forEach {
						FromJSON.createNote(it)?.apply {
							val time = LocalDate.ofInstant(Instant.ofEpochSecond(time * 60), systemDefault())
							
							if(!tmpDayNotes.containsKey(time.dayOfMonth))
								tmpDayNotes[time.dayOfMonth] = mutableListOf()
							
							tmpDayNotes[time.dayOfMonth]!!.add(this)
							log("loaded Day Note: $this", LogType.LOW)
						}
					}
					log("loaded temp day Notes $tmpDayNotes", LogType.NORMAL)
				}
			}
		}
	
	tmpDayNotes[noteTime.dayOfMonth]?.removeIf { it.time == note.time && it.type == note.type }
	
	if(!tmpDayNotes.containsKey(noteTime.dayOfMonth))
		tmpDayNotes[noteTime.dayOfMonth] = mutableListOf()
	
	tmpDayNotes[noteTime.dayOfMonth]!!.add(note)
	log("new day Notes $tmpDayNotes", LogType.NORMAL)
	
	File(ConfigFiles.notesDir + "/${noteTime.month.name}.json").run {
		val original =
			Json().fromJson<Map<String, ArrayList<Map<String, Any>>>>(getJsonReader(FileReader(ConfigFiles.notesDir + "/${noteTime.month.name}.json")), Map::class.java).toMutableMap()
		
		val list = mutableListOf<Map<String, Any>>()
		tmpDayNotes.forEach { list.addAll(it.value.map { note -> ToJson.createNote(note) }) }
		
		original["Day Notes"] = list as ArrayList<Map<String, Any>>
		
		writeText(Json().toJson(original))
	}
}

fun removeDayNote(note: Note) {
	val noteTime = LocalDate.ofInstant(Instant.ofEpochSecond(note.time * 60), systemDefault())
	File(ConfigFiles.notesDir + "/${noteTime.month.name}.json").run {
		if(!exists()) {
			log("file with notes for ${noteTime.month.name} not found", LogType.LOW)
			createNewFile()
			writeText("{\n\t\"Week Notes\": [],\n\t\"Day Notes\": []\n}")
		}
	}
	
	val tmpDayNotes: MutableMap<Int, MutableList<Note>> = mutableMapOf()
	
	Json().fromJson<Map<String, ArrayList<Map<String, Any>>>>(getJsonReader(FileReader(ConfigFiles.notesDir + "/${noteTime.month.name}.json")), Map::class.java)
		.forEach { (name, list) ->
			when(name) {
				"Day Notes" -> {
					log("reading Day Notes", LogType.LOW)
					list.forEach {
						FromJSON.createNote(it)?.apply {
							val time = LocalDate.ofInstant(Instant.ofEpochSecond(time * 60), systemDefault())
							
							if(!tmpDayNotes.containsKey(time.dayOfMonth))
								tmpDayNotes[time.dayOfMonth] = mutableListOf()
							
							tmpDayNotes[time.dayOfMonth]!!.add(this)
							log("loaded Day Note: $this", LogType.LOW)
						}
					}
					log("loaded temp day Notes $tmpDayNotes", LogType.NORMAL)
				}
			}
		}
	
	tmpDayNotes[noteTime.dayOfMonth]?.removeIf { it.time == note.time && it.type == note.type }

	log("new day Notes $tmpDayNotes", LogType.NORMAL)
	
	File(ConfigFiles.notesDir + "/${noteTime.month.name}.json").run {
		val original =
			Json().fromJson<Map<String, ArrayList<Map<String, Any>>>>(getJsonReader(FileReader(ConfigFiles.notesDir + "/${noteTime.month.name}.json")), Map::class.java).toMutableMap()
		
		val list = mutableListOf<Map<String, Any>>()
		tmpDayNotes.forEach { list.addAll(it.value.map { note -> ToJson.createNote(note) }) }
		
		original["Day Notes"] = list as ArrayList<Map<String, Any>>
		
		writeText(Json().toJson(original))
	}
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
		prepareMonthAppointments(calendarDisplay.month.minus(1))
		prepareMonthNotes(calendarDisplay.month.minus(1))
		prepareMonthAppointments(calendarDisplay.month)
		prepareMonthNotes(calendarDisplay.month)
		prepareMonthAppointments(calendarDisplay.month.plus(1))
		prepareMonthNotes(calendarDisplay.month.plus(1))
	}
	
	val data = generateMonth(calendarDisplay)
	currentMonth.clear()
	currentMonth.addAll(data)
}


private fun generateMonth(monthTime: ZonedDateTime): MutableList<Week> {
	log("generating Month", LogType.LOW)
	var time: ZonedDateTime = monthTime.withDayOfMonth(1)
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
	} while(time.month == monthTime.month && time.dayOfMonth > 1)
	
	return weeks
}



// Day: { Appointments }
val preparedweeklyAppointments: MutableMap<DayOfWeek, MutableList<Appointment>> = mutableMapOf()

// ----overridden with each Month change----

// Month: { Day of month: Appointments }
val preparedsingleAppointments: MutableMap<Month, MutableMap<Int, MutableList<Appointment>>> = mutableMapOf()

// Month: { Day of month: Appointments }
val preparedDayNotes: MutableMap<Month, MutableMap<Int, MutableList<Note>>> = mutableMapOf()

// Month: { Week of year: Appointments }
val preparedWeekNotes: MutableMap<Month, MutableMap<Int, MutableList<Note>>> = mutableMapOf()



fun prepareWeekAppointments() {
	File(ConfigFiles.weekAppointmentsFile).run {
		if(!exists()) {
			createNewFile()
			writeText(emptyDefault)
			log("created default weekAppointmentsFile:${ConfigFiles.weekAppointmentsFile}", LogType.WARNING)
		}
	}
	Json().fromJson<ArrayList<Map<String, Any>>>(getJsonReader(FileReader(ConfigFiles.weekAppointmentsFile)), List::class.java).forEach { list ->
		log("reading Week Appointment: $list", LogType.LOW)
		FromJSON.createAppointment(list, true)?.run {
			if(!preparedweeklyAppointments.containsKey(day))
				preparedweeklyAppointments[day] = mutableListOf()
			
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
private fun prepareMonthAppointments(Month: Month) {
	File(ConfigFiles.appointmentsDir + "/$Month.json").run {
		if(!exists()) {
			log("file with appointments for $Month not found", LogType.LOW)
			return
		}
	}
	Json().fromJson<ArrayList<Map<String, Any>>>(getJsonReader(FileReader(ConfigFiles.appointmentsDir + "/$Month.json")), List::class.java).forEach { list ->
		log("reading single Appointment: $list", LogType.LOW)
		FromJSON.createAppointment(list, false)?.run {
			val time = LocalDate.ofInstant(Instant.ofEpochSecond(start * 60), systemDefault())
			
			val offset: ZoneOffset = systemDefault().rules.getOffset(Instant.ofEpochSecond(start * 60))
			start -= time.atStartOfDay().toLocalTime().toEpochSecond(time, offset) / 60
			day = time.dayOfWeek
			if(!preparedsingleAppointments.containsKey(time.month))
				preparedsingleAppointments[time.month] = mutableMapOf()
			if(!preparedsingleAppointments[time.month]!!.containsKey(time.dayOfMonth))
				preparedsingleAppointments[time.month]!![time.dayOfMonth] = mutableListOf()
			
			preparedsingleAppointments[time.month]!![time.dayOfMonth]!!.add(this)
			log("loaded single Appointment: $this", LogType.LOW)
		}
	}
	log("prepared single Appointments $preparedsingleAppointments", LogType.NORMAL)
}


private fun prepareMonthNotes(Month: Month) {
	File(ConfigFiles.notesDir + "/$Month.json").run {
		if(!exists()) {
			log("file with notes for $Month not found", LogType.LOW)
			return
		}
	}
	Json().fromJson<Map<String, ArrayList<Map<String, Any>>>>(getJsonReader(FileReader(ConfigFiles.notesDir + "/$Month.json")), Map::class.java).forEach { (name, list) ->
		when(name) {
			"Day Notes" -> {
				log("reading Day Notes", LogType.LOW)
				list.forEach {
					FromJSON.createNote(it)?.apply {
						val time = LocalDate.ofInstant(Instant.ofEpochSecond(time * 60), systemDefault())
						
						if(!preparedDayNotes.containsKey(time.month))
							preparedDayNotes[time.month] = mutableMapOf()
						if(!preparedDayNotes[time.month]!!.containsKey(time.dayOfMonth))
							preparedDayNotes[time.month]!![time.dayOfMonth] = mutableListOf()
						
						preparedDayNotes[time.month]!![time.dayOfMonth]!!.add(this)
						log("loaded Day Note: $this", LogType.LOW)
					}
				}
				log("prepared day Notes $preparedDayNotes", LogType.NORMAL)
			}
			"Week Notes" -> {
				log("reading Week Notes", LogType.LOW)
				list.forEach {
					FromJSON.createNote(it)?.apply {
						val time = LocalDate.ofInstant(Instant.ofEpochSecond(time * 60), systemDefault())
						
						if(!preparedWeekNotes.containsKey(time.month))
							preparedWeekNotes[time.month] = mutableMapOf()
						if(!preparedWeekNotes[time.month]!!.containsKey(time.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)))
							preparedWeekNotes[time.month]!![time.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)] = mutableListOf()
						
						preparedWeekNotes[time.month]!![time.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)]!!.add(this)
						log("loaded week Note: $this", LogType.LOW)
					}
				}
				log("prepared Week Notes $preparedWeekNotes", LogType.NORMAL)
			}
		}
	}
	
}
