package calendar

import logic.getJson as Json
import javafx.beans.property.*
import javafx.collections.*
import logic.ConfigFiles
import logic.LogType
import logic.emptydefault
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

var calendardisplay: ZonedDateTime = ZonedDateTime.now(systemDefault())

val currentmonth: ObservableList<Week> = FXCollections.observableArrayList()
val currentmonthName: SimpleStringProperty = SimpleStringProperty()

/**
 * called by buttons in calendar tab
 * and on startup
 */
fun changeMonth(right: Boolean) {
	calendardisplay = calendardisplay.plusMonths(if(right) 1 else -1)
	
	currentmonthName.set(getLangString(calendardisplay.month.name))
	if(calendardisplay.year != now.year)
		currentmonthName.value += "  " + calendardisplay.year
	log("changing Month to ${calendardisplay.month.name}")
	
	prepareddayNotes.remove(calendardisplay.month.minus(if(right) 2 else -2))
	preparedweekNotes.remove(calendardisplay.month.minus(if(right) 2 else -2))
	preparedsingleAppointments.remove(calendardisplay.month.minus(if(right) 2 else -2))
	
	prepareMonthAppointments(calendardisplay.month.plus(if(right) 1 else -1))
	preapareMonthNotes(calendardisplay.month.plus(if(right) 1 else -1))
	
	val data = generateMonth(calendardisplay)
	currentmonth.clear()
	currentmonth.addAll(data)
}

fun saveDayNote(note: Note) {
	val notetime = LocalDate.ofInstant(Instant.ofEpochSecond(note.time * 60), systemDefault())
	File(ConfigFiles.notesdir + "/${notetime.month.name}.json").run {
		if(!exists()) {
			log("file with notes for ${notetime.month.name} not found", LogType.LOW)
			createNewFile()
			writeText("{\n\t\"Week Notes\": [],\n\t\"Day Notes\": []\n}")
		}
	}
	
	val tmpdaynotes: MutableMap<Int, MutableList<Note>> = mutableMapOf()
	
	Json().fromJson<Map<String, ArrayList<Map<String, Any>>>>(getJsonReader(FileReader(ConfigFiles.notesdir + "/${notetime.month.name}.json")), Map::class.java)
		.forEach { (name, list) ->
			when(name) {
				"Day Notes" -> {
					log("reading Day Notes", LogType.LOW)
					list.forEach {
						FromJSON.createNote(it)?.apply {
							val time = LocalDate.ofInstant(Instant.ofEpochSecond(time * 60), systemDefault())
							
							if(!tmpdaynotes.containsKey(time.dayOfMonth))
								tmpdaynotes[time.dayOfMonth] = mutableListOf()
							
							tmpdaynotes[time.dayOfMonth]!!.add(this)
							log("loaded Day Note: $this", LogType.LOW)
						}
					}
					log("loaded temp day Notes $tmpdaynotes", LogType.NORMAL)
				}
			}
		}
	
	tmpdaynotes[notetime.dayOfMonth]?.removeIf { it.time == note.time && it.type == note.type }
	
	if(!tmpdaynotes.containsKey(notetime.dayOfMonth))
		tmpdaynotes[notetime.dayOfMonth] = mutableListOf()
	
	tmpdaynotes[notetime.dayOfMonth]!!.add(note)
	log("new day Notes $tmpdaynotes", LogType.NORMAL)
	
	File(ConfigFiles.notesdir + "/${notetime.month.name}.json").run {
		val original =
			Json().fromJson<Map<String, ArrayList<Map<String, Any>>>>(getJsonReader(FileReader(ConfigFiles.notesdir + "/${notetime.month.name}.json")), Map::class.java).toMutableMap()
		
		val list = mutableListOf<Map<String, Any>>()
		tmpdaynotes.forEach { list.addAll(it.value.map { ToJson.createNote(it) }) }
		
		original["Day Notes"] = list as ArrayList<Map<String, Any>>
		
		writeText(Json().toJson(original))
	}
}

fun removeDayNote(note: Note) {
	val notetime = LocalDate.ofInstant(Instant.ofEpochSecond(note.time * 60), systemDefault())
	File(ConfigFiles.notesdir + "/${notetime.month.name}.json").run {
		if(!exists()) {
			log("file with notes for ${notetime.month.name} not found", LogType.LOW)
			createNewFile()
			writeText("{\n\t\"Week Notes\": [],\n\t\"Day Notes\": []\n}")
		}
	}
	
	val tmpdaynotes: MutableMap<Int, MutableList<Note>> = mutableMapOf()
	
	Json().fromJson<Map<String, ArrayList<Map<String, Any>>>>(getJsonReader(FileReader(ConfigFiles.notesdir + "/${notetime.month.name}.json")), Map::class.java)
		.forEach { (name, list) ->
			when(name) {
				"Day Notes" -> {
					log("reading Day Notes", LogType.LOW)
					list.forEach {
						FromJSON.createNote(it)?.apply {
							val time = LocalDate.ofInstant(Instant.ofEpochSecond(time * 60), systemDefault())
							
							if(!tmpdaynotes.containsKey(time.dayOfMonth))
								tmpdaynotes[time.dayOfMonth] = mutableListOf()
							
							tmpdaynotes[time.dayOfMonth]!!.add(this)
							log("loaded Day Note: $this", LogType.LOW)
						}
					}
					log("loaded temp day Notes $tmpdaynotes", LogType.NORMAL)
				}
			}
		}
	
	tmpdaynotes[notetime.dayOfMonth]?.removeIf { it.time == note.time && it.type == note.type }

	log("new day Notes $tmpdaynotes", LogType.NORMAL)
	
	File(ConfigFiles.notesdir + "/${notetime.month.name}.json").run {
		val original =
			Json().fromJson<Map<String, ArrayList<Map<String, Any>>>>(getJsonReader(FileReader(ConfigFiles.notesdir + "/${notetime.month.name}.json")), Map::class.java).toMutableMap()
		
		val list = mutableListOf<Map<String, Any>>()
		tmpdaynotes.forEach { list.addAll(it.value.map { ToJson.createNote(it) }) }
		
		original["Day Notes"] = list as ArrayList<Map<String, Any>>
		
		writeText(Json().toJson(original))
	}
}

/**
 * called at initialization
 * and loads 3 months and week appointments
 */
fun loadCalendarData() {
	currentmonthName.set(getLangString(calendardisplay.month.name))
	if(calendardisplay.year != now.year)
		currentmonthName.value += "  " + calendardisplay.year
	log("set Month to ${calendardisplay.month.name}")
	
	preparedweeklyAppointments.clear()
	prepareddayNotes.clear()
	preparedweekNotes.clear()
	preparedsingleAppointments.clear()
	
	run {
		prepareWeekAppointments()
		prepareMonthAppointments(calendardisplay.month.minus(1))
		preapareMonthNotes(calendardisplay.month.minus(1))
		prepareMonthAppointments(calendardisplay.month)
		preapareMonthNotes(calendardisplay.month)
		prepareMonthAppointments(calendardisplay.month.plus(1))
		preapareMonthNotes(calendardisplay.month.plus(1))
	}
	
	val data = generateMonth(calendardisplay)
	currentmonth.clear()
	currentmonth.addAll(data)
}


private fun generateMonth(monthtime: ZonedDateTime): MutableList<Week> {
	log("generating Month", LogType.LOW)
	var time: ZonedDateTime = monthtime.withDayOfMonth(1)
	val month = time.month
	
	val dayoffset = time.dayOfWeek.value
	time = time.minusDays((dayoffset - 1).toLong())
	
	val weeks: MutableList<Week> = mutableListOf()
	
	do {
		val days: MutableList<Day> = mutableListOf()
		do {
			val day = Day(time, time.month == month)
			day.appointments.addAll(preparedsingleAppointments[time.month]?.get(time.dayOfMonth) ?: listOf())
			day.notes.addAll(prepareddayNotes[time.month]?.get(time.dayOfMonth) ?: listOf())
			
			days.add(day)
			time = time.plusDays(1)
		} while(time.dayOfWeek.value != 1)
		
		val starttime = time.minusDays(7)
		
		val week = Week(
			starttime,
			days[0], days[1], days[2], days[3], days[4], days[5], days[6],
			starttime.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
		)
		week.addAppointments(preparedweeklyAppointments)
		week.notes.addAll(preparedweekNotes[starttime.month]?.get(week.time.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)) ?: listOf())
		
		log("added week: $week", LogType.LOW)
		weeks.add(week)
	} while(time.month == monthtime.month && time.dayOfMonth > 1)
	
	return weeks
}



// Day: { Appointments }
val preparedweeklyAppointments: MutableMap<DayOfWeek, MutableList<Appointment>> = mutableMapOf()

// ----overridden with each Month change----

// Month: { Day of month: Appointments }
val preparedsingleAppointments: MutableMap<Month, MutableMap<Int, MutableList<Appointment>>> = mutableMapOf()

// Month: { Day of month: Appointments }
val prepareddayNotes: MutableMap<Month, MutableMap<Int, MutableList<Note>>> = mutableMapOf()

// Month: { Week of year: Appointments }
val preparedweekNotes: MutableMap<Month, MutableMap<Int, MutableList<Note>>> = mutableMapOf()



fun prepareWeekAppointments() {
	File(ConfigFiles.weekappointmentsfile).run {
		if(!exists()) {
			createNewFile()
			writeText(emptydefault)
			log("created default weekappointmentsfile:${ConfigFiles.weekappointmentsfile}", LogType.WARNING)
		}
	}
	Json().fromJson<ArrayList<Map<String, Any>>>(getJsonReader(FileReader(ConfigFiles.weekappointmentsfile)), List::class.java).forEach { list ->
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
	File(ConfigFiles.appointmentsdir + "/$Month.json").run {
		if(!exists()) {
			log("file with appointments for $Month not found", LogType.LOW)
			return
		}
	}
	Json().fromJson<ArrayList<Map<String, Any>>>(getJsonReader(FileReader(ConfigFiles.appointmentsdir + "/$Month.json")), List::class.java).forEach { list ->
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


private fun preapareMonthNotes(Month: Month) {
	File(ConfigFiles.notesdir + "/$Month.json").run {
		if(!exists()) {
			log("file with notes for $Month not found", LogType.LOW)
			return
		}
	}
	Json().fromJson<Map<String, ArrayList<Map<String, Any>>>>(getJsonReader(FileReader(ConfigFiles.notesdir + "/$Month.json")), Map::class.java).forEach { (name, list) ->
		when(name) {
			"Day Notes" -> {
				log("reading Day Notes", LogType.LOW)
				list.forEach {
					FromJSON.createNote(it)?.apply {
						val time = LocalDate.ofInstant(Instant.ofEpochSecond(time * 60), systemDefault())
						
						if(!prepareddayNotes.containsKey(time.month))
							prepareddayNotes[time.month] = mutableMapOf()
						if(!prepareddayNotes[time.month]!!.containsKey(time.dayOfMonth))
							prepareddayNotes[time.month]!![time.dayOfMonth] = mutableListOf()
						
						prepareddayNotes[time.month]!![time.dayOfMonth]!!.add(this)
						log("loaded Day Note: $this", LogType.LOW)
					}
				}
				log("prepared day Notes $prepareddayNotes", LogType.NORMAL)
			}
			"Week Notes" -> {
				log("reading Week Notes", LogType.LOW)
				list.forEach {
					FromJSON.createNote(it)?.apply {
						val time = LocalDate.ofInstant(Instant.ofEpochSecond(time * 60), systemDefault())
						
						if(!preparedweekNotes.containsKey(time.month))
							preparedweekNotes[time.month] = mutableMapOf()
						if(!preparedweekNotes[time.month]!!.containsKey(time.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)))
							preparedweekNotes[time.month]!![time.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)] = mutableListOf()
						
						preparedweekNotes[time.month]!![time.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)]!!.add(this)
						log("loaded week Note: $this", LogType.LOW)
					}
				}
				log("prepared Week Notes $preparedweekNotes", LogType.NORMAL)
			}
		}
	}
	
}
