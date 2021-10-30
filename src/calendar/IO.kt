package calendar

import logic.ConfigFiles
import logic.LogType
import logic.getJson
import logic.getJsonReader
import logic.log
import java.io.File
import java.io.FileReader
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.IsoFields
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set


fun saveDayNote(note: Note) {
	val noteTime = LocalDate.ofInstant(Instant.ofEpochSecond(note.time * 60), ZoneOffset.UTC)
	File(ConfigFiles.notesDir + "/${noteTime.month.name}.json").run {
		if(!exists()) {
			log("file with notes for ${noteTime.month.name} not found", LogType.LOW)
			createNewFile()
			writeText("{\n\t\"Week Notes\": [],\n\t\"Day Notes\": []\n}")
		}
	}
	
	val tmpDayNotes: MutableMap<Int, MutableList<Note>> = mutableMapOf()
	
	getJson().fromJson<Map<String, ArrayList<Map<String, Any>>>>(getJsonReader(FileReader(ConfigFiles.notesDir + "/${noteTime.month.name}.json")), Map::class.java)
		.forEach { (name, list) ->
			when(name) {
				"Day Notes" -> {
					log("reading Day Notes", LogType.LOW)
					list.forEach {
						Note.fromJSON<Note>(it)?.apply {
							val time = LocalDate.ofInstant(Instant.ofEpochSecond(time * 60), ZoneOffset.UTC)
							
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
	
	tmpDayNotes[noteTime.dayOfMonth]?.removeIf { it.id == note.id }
	
	if(!tmpDayNotes.containsKey(noteTime.dayOfMonth))
		tmpDayNotes[noteTime.dayOfMonth] = mutableListOf()
	
	tmpDayNotes[noteTime.dayOfMonth]!!.add(note)
	log("new day Notes $tmpDayNotes", LogType.NORMAL)
	
	File(ConfigFiles.notesDir + "/${noteTime.month.name}.json").run {
		val original =
			getJson().fromJson<Map<String, ArrayList<Map<String, Any>>>>(getJsonReader(FileReader(ConfigFiles.notesDir + "/${noteTime.month.name}.json")), Map::class.java)
				.toMutableMap()
		
		val list = mutableListOf<Map<String, Any>>()
		tmpDayNotes.forEach { list.addAll(it.value.map { note -> note.toJSON() }) }
		
		original["Day Notes"] = list as ArrayList<Map<String, Any>>
		
		writeText(getJson().toJson(original))
	}
}

fun removeDayNote(note: Note) {
	val noteTime = LocalDate.ofInstant(Instant.ofEpochSecond(note.time * 60), ZoneOffset.UTC)
	File(ConfigFiles.notesDir + "/${noteTime.month.name}.json").run {
		if(!exists()) {
			log("file with notes for ${noteTime.month.name} not found", LogType.LOW)
			createNewFile()
			writeText("{\n\t\"Week Notes\": [],\n\t\"Day Notes\": []\n}")
		}
	}
	
	val tmpDayNotes: MutableMap<Int, MutableList<Note>> = mutableMapOf()
	
	getJson().fromJson<Map<String, ArrayList<Map<String, Any>>>>(getJsonReader(FileReader(ConfigFiles.notesDir + "/${noteTime.month.name}.json")), Map::class.java)
		.forEach { (name, list) ->
			when(name) {
				"Day Notes" -> {
					log("reading Day Notes", LogType.LOW)
					list.forEach {
						Note.fromJSON<Note>(it)?.apply {
							val time = LocalDate.ofInstant(Instant.ofEpochSecond(time * 60), ZoneOffset.UTC)
							
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
	
	tmpDayNotes[noteTime.dayOfMonth]?.removeIf { it.id == note.id }
	
	log("new day Notes $tmpDayNotes", LogType.NORMAL)
	
	File(ConfigFiles.notesDir + "/${noteTime.month.name}.json").run {
		val original =
			getJson().fromJson<Map<String, ArrayList<Map<String, Any>>>>(getJsonReader(FileReader(ConfigFiles.notesDir + "/${noteTime.month.name}.json")), Map::class.java)
				.toMutableMap()
		
		val list = mutableListOf<Map<String, Any>>()
		tmpDayNotes.forEach { list.addAll(it.value.map { note -> note.toJSON() }) }
		
		original["Day Notes"] = list as ArrayList<Map<String, Any>>
		
		writeText(getJson().toJson(original))
	}
}

fun saveWeekNote(note: Note) {
	val noteTime = LocalDate.ofInstant(Instant.ofEpochSecond(note.time * 60), ZoneOffset.UTC)
	File(ConfigFiles.notesDir + "/${noteTime.month.name}.json").run {
		if(!exists()) {
			log("file with notes for ${noteTime.month.name} not found", LogType.LOW)
			createNewFile()
			writeText("{\n\t\"Week Notes\": [],\n\t\"Day Notes\": []\n}")
		}
	}
	
	val tmpWeekNotes: MutableMap<Int, MutableList<Note>> = mutableMapOf()
	
	getJson().fromJson<Map<String, ArrayList<Map<String, Any>>>>(getJsonReader(FileReader(ConfigFiles.notesDir + "/${noteTime.month.name}.json")), Map::class.java)
		.forEach { (name, list) ->
			when(name) {
				"Week Notes" -> {
					log("reading Week Notes", LogType.LOW)
					list.forEach {
						Note.fromJSON<Note>(it)?.apply {
							val time = LocalDate.ofInstant(Instant.ofEpochSecond(time * 60), ZoneOffset.UTC)
							
							if(!tmpWeekNotes.containsKey(time.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)))
								tmpWeekNotes[time.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)] = mutableListOf()
							
							tmpWeekNotes[time.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)]!!.add(this)
							log("loaded week Note: $this", LogType.LOW)
						}
					}
					log("loaded temp Week Notes $tmpWeekNotes", LogType.NORMAL)
				}
			}
		}
	
	tmpWeekNotes[noteTime.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)]?.removeIf { it.id == note.id }
	
	if(!tmpWeekNotes.containsKey(noteTime.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)))
		tmpWeekNotes[noteTime.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)] = mutableListOf()
	
	tmpWeekNotes[noteTime.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)]!!.add(note)
	log("new Week Notes $tmpWeekNotes", LogType.NORMAL)
	
	File(ConfigFiles.notesDir + "/${noteTime.month.name}.json").run {
		val original =
			getJson().fromJson<Map<String, ArrayList<Map<String, Any>>>>(getJsonReader(FileReader(ConfigFiles.notesDir + "/${noteTime.month.name}.json")), Map::class.java)
				.toMutableMap()
		
		val list = mutableListOf<Map<String, Any>>()
		tmpWeekNotes.forEach { list.addAll(it.value.map { note -> note.toJSON() }) }
		
		original["Week Notes"] = list as ArrayList<Map<String, Any>>
		
		writeText(getJson().toJson(original))
	}
}

fun removeWeekNote(note: Note) {
	val noteTime = LocalDate.ofInstant(Instant.ofEpochSecond(note.time * 60), ZoneOffset.UTC)
	File(ConfigFiles.notesDir + "/${noteTime.month.name}.json").run {
		if(!exists()) {
			log("file with notes for ${noteTime.month.name} not found", LogType.LOW)
			createNewFile()
			writeText("{\n\t\"Week Notes\": [],\n\t\"Day Notes\": []\n}")
		}
	}
	
	val tmpWeekNotes: MutableMap<Int, MutableList<Note>> = mutableMapOf()
	
	getJson().fromJson<Map<String, ArrayList<Map<String, Any>>>>(getJsonReader(FileReader(ConfigFiles.notesDir + "/${noteTime.month.name}.json")), Map::class.java)
		.forEach { (name, list) ->
			when(name) {
				"Week Notes" -> {
					log("reading Week Notes", LogType.LOW)
					list.forEach {
						Note.fromJSON<Note>(it)?.apply {
							val time = LocalDate.ofInstant(Instant.ofEpochSecond(time * 60), ZoneOffset.UTC)
							
							if(!tmpWeekNotes.containsKey(time.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)))
								tmpWeekNotes[time.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)] = mutableListOf()
							
							tmpWeekNotes[time.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)]!!.add(this)
							log("loaded week Note: $this", LogType.LOW)
						}
					}
					log("loaded temp Week Notes $tmpWeekNotes", LogType.NORMAL)
				}
			}
		}
	
	tmpWeekNotes[noteTime.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)]?.removeIf { it.id == note.id }
	
	log("new Week Notes $tmpWeekNotes", LogType.NORMAL)
	
	File(ConfigFiles.notesDir + "/${noteTime.month.name}.json").run {
		val original =
			getJson().fromJson<Map<String, ArrayList<Map<String, Any>>>>(getJsonReader(FileReader(ConfigFiles.notesDir + "/${noteTime.month.name}.json")), Map::class.java)
				.toMutableMap()
		
		val list = mutableListOf<Map<String, Any>>()
		tmpWeekNotes.forEach { list.addAll(it.value.map { note -> note.toJSON() }) }
		
		original["Week Notes"] = list as ArrayList<Map<String, Any>>
		
		writeText(getJson().toJson(original))
	}
}



fun saveDayAppointment(appointment: Appointment) {
	val appointmentTime = LocalDate.ofInstant(Instant.ofEpochSecond(appointment.start * 60), ZoneOffset.UTC)
	
	File(ConfigFiles.appointmentsDir + "/${appointmentTime.month.name}.json").run {
		if(!exists()) {
			log("file with notes for ${appointmentTime.month.name} not found", LogType.LOW)
			createNewFile()
			writeText("[\n\t\n]")
		}
	}
	
	val tmpDayAppointments: MutableMap<Int, MutableList<Appointment>> = mutableMapOf()
	
	getJson().fromJson<ArrayList<Map<String, Any>>>(getJsonReader(FileReader(ConfigFiles.appointmentsDir + "/${appointmentTime.month.name}.json")), List::class.java)
		.forEach { list ->
			log("reading Appointments", LogType.LOW)
			Appointment.fromJSON<Appointment>(list)?.apply {
				val time = LocalDate.ofInstant(Instant.ofEpochSecond(appointment.start * 60), ZoneOffset.UTC)
				
				if(!tmpDayAppointments.containsKey(time.dayOfMonth))
					tmpDayAppointments[time.dayOfMonth] = mutableListOf()
				
				tmpDayAppointments[time.dayOfMonth]!!.add(this)
				log("loaded Day Note: $this", LogType.LOW)
			}
		}
	
	log("loaded temp Day Appointments $tmpDayAppointments", LogType.NORMAL)
	
	// removes duplicate / overrides old
	tmpDayAppointments[appointmentTime.dayOfMonth]?.removeIf { it.id == appointment.id }
	
	if(!tmpDayAppointments.containsKey(appointmentTime.dayOfMonth))
		tmpDayAppointments[appointmentTime.dayOfMonth] = mutableListOf()
	
	tmpDayAppointments[appointmentTime.dayOfMonth]!!.add(appointment)
	
	log("new day Appointments $tmpDayAppointments", LogType.NORMAL)
	
	File(ConfigFiles.appointmentsDir + "/${appointmentTime.month.name}.json").run {
		val original =
			getJson().fromJson<ArrayList<Map<String, Any>>>(getJsonReader(FileReader(ConfigFiles.appointmentsDir + "/${appointmentTime.month.name}.json")), List::class.java)
				.toMutableList()
		
		tmpDayAppointments.forEach { original.addAll(it.value.map { app -> app.toJSON() }) }
		
		writeText(getJson().toJson(original))
	}
}

// TODO implement
fun createID(): Long {
	log("id for generated")
	return -1
}