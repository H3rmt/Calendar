package calendar

import calendar.Timing.UTCEpochMinuteToLocalDateTime
import logic.ConfigFiles
import logic.Exit
import logic.LogType
import logic.getJson
import logic.getJsonReader
import logic.log
import java.io.File
import java.io.FileReader
import java.time.temporal.IsoFields


fun saveDayNote(note: Note, add: Boolean = true) {
	log("${if(add) "Adding" else "Removing"} Day Note $note", LogType.IMPORTANT)
	val noteTime = UTCEpochMinuteToLocalDateTime(note.time)
	
	File(ConfigFiles.notesDir + "/${noteTime.month.name}.json").run {
		if(!exists()) {
			log("file with notes for ${noteTime.month.name} not found", LogType.LOW)
			createNewFile()
			writeText("{\n\t\"Week Notes\": [],\n\t\"Day Notes\": []\n}")
		}
	}
	
	resetGroup(IDGroups.Notes)
	val tmpDayNotes: MutableMap<Int, MutableList<Note>> = mutableMapOf()
	
	getJson().fromJson<Map<String, ArrayList<Map<String, Any>>>>(getJsonReader(FileReader(ConfigFiles.notesDir + "/${noteTime.month.name}.json")), Map::class.java)
		.forEach { (name, list) ->
			when(name) {
				"Day Notes" -> {
					log("reading Day Notes", LogType.LOW)
					list.forEach {
						Note.fromJSON<Note>(it)?.apply {
							val time = UTCEpochMinuteToLocalDateTime(time)
							
							if(!tmpDayNotes.containsKey(time.dayOfMonth))
								tmpDayNotes[time.dayOfMonth] = mutableListOf()
							
							tmpDayNotes[time.dayOfMonth]!!.add(this)
							
							usedID(IDGroups.Notes, id)
							log("loaded Day Note: $this", LogType.LOW)
						}
					}
					log("loaded old day Notes $tmpDayNotes", LogType.NORMAL)
				}
			}
		}
	
	tmpDayNotes[noteTime.dayOfMonth]?.removeIf { it.id == note.id }
	
	if(add) {
		if(!tmpDayNotes.containsKey(noteTime.dayOfMonth))
			tmpDayNotes[noteTime.dayOfMonth] = mutableListOf()
		
		tmpDayNotes[noteTime.dayOfMonth]!!.add(note)
	}
	
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

fun saveWeekNote(note: Note, add: Boolean = true) {
	log("${if(add) "Adding" else "Removing"} Week Note $note", LogType.IMPORTANT)
	val noteTime = UTCEpochMinuteToLocalDateTime(note.time)
	
	File(ConfigFiles.notesDir + "/${noteTime.month.name}.json").run {
		if(!exists()) {
			log("file with notes for ${noteTime.month.name} not found", LogType.LOW)
			createNewFile()
			writeText("{\n\t\"Week Notes\": [],\n\t\"Day Notes\": []\n}")
		}
	}
	
	resetGroup(IDGroups.Notes)
	val tmpWeekNotes: MutableMap<Int, MutableList<Note>> = mutableMapOf()
	
	getJson().fromJson<Map<String, ArrayList<Map<String, Any>>>>(getJsonReader(FileReader(ConfigFiles.notesDir + "/${noteTime.month.name}.json")), Map::class.java)
		.forEach { (name, list) ->
			when(name) {
				"Week Notes" -> {
					log("reading Week Notes", LogType.LOW)
					list.forEach {
						Note.fromJSON<Note>(it)?.apply {
							val time = UTCEpochMinuteToLocalDateTime(time)
							
							if(!tmpWeekNotes.containsKey(time.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)))
								tmpWeekNotes[time.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)] = mutableListOf()
							
							tmpWeekNotes[time.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)]!!.add(this)
							
							usedID(IDGroups.Notes, id)
							log("loaded week Note: $this", LogType.LOW)
						}
					}
					log("loaded old Week Notes $tmpWeekNotes", LogType.NORMAL)
				}
			}
		}
	
	tmpWeekNotes[noteTime.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)]?.removeIf { it.id == note.id }
	
	if(add) {
		if(!tmpWeekNotes.containsKey(noteTime.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)))
			tmpWeekNotes[noteTime.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)] = mutableListOf()
		
		tmpWeekNotes[noteTime.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)]!!.add(note)
	}
	
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


fun saveDayAppointment(appointment: Appointment, add: Boolean = true) {
	log("${if(add) "Adding" else "Removing"} Day Appointment $appointment", LogType.IMPORTANT)
	val appointmentTime = UTCEpochMinuteToLocalDateTime(appointment.start)
	
	File(ConfigFiles.appointmentsDir + "/${appointmentTime.month.name}.json").run {
		if(!exists()) {
			log("file with notes for ${appointmentTime.month.name} not found", LogType.WARNING)
			createNewFile()
			writeText("[\n\t\n]")
		}
	}
	
	resetGroup(IDGroups.Appointments)
	val tmpDayAppointments: MutableMap<Int, MutableList<Appointment>> = mutableMapOf()
	
	getJson().fromJson<ArrayList<Map<String, Any>>>(getJsonReader(FileReader(ConfigFiles.appointmentsDir + "/${appointmentTime.month.name}.json")), List::class.java)
		.forEach { list ->
			log("reading Appointments", LogType.LOW)
			Appointment.fromJSON<Appointment>(list)?.apply {
				val time = UTCEpochMinuteToLocalDateTime(appointment.start)
				
				if(!tmpDayAppointments.containsKey(time.dayOfMonth))
					tmpDayAppointments[time.dayOfMonth] = mutableListOf()
				
				tmpDayAppointments[time.dayOfMonth]!!.add(this)
				
				usedID(IDGroups.Appointments, id)
				log("loaded Day Note: $this", LogType.LOW)
			}
		}
	
	log("loaded old Day Appointments $tmpDayAppointments", LogType.NORMAL)
	
	// removes duplicate / overrides old
	tmpDayAppointments[appointmentTime.dayOfMonth]?.removeIf { it.id == appointment.id }
	
	if(add) {
		if(!tmpDayAppointments.containsKey(appointmentTime.dayOfMonth))
			tmpDayAppointments[appointmentTime.dayOfMonth] = mutableListOf()
		
		tmpDayAppointments[appointmentTime.dayOfMonth]!!.add(appointment)
	}
	
	log("new day Appointments $tmpDayAppointments", LogType.NORMAL)
	
	File(ConfigFiles.appointmentsDir + "/${appointmentTime.month.name}.json").run {
		val list = mutableListOf<Map<String, Any>>()
		tmpDayAppointments.forEach { list.addAll(it.value.map { note -> note.toJSON() }) }
		
		writeText(getJson().toJson(list))
	}
}

val idstorage: MutableMap<IDGroups, MutableList<Long>> = mutableMapOf(
	IDGroups.Appointments to mutableListOf(),
	IDGroups.Notes to mutableListOf(),
	IDGroups.Files to mutableListOf()
)

enum class IDGroups {
	Appointments,
	Notes,
	Files
}

fun resetGroup(group: IDGroups) {
	idstorage[group]!!.clear()
}

fun usedID(group: IDGroups, id: Long) {
	if(!idstorage[group]!!.contains(id)) {
		idstorage[group]!!.add(id)
		log("added ID $id for group $group: ${idstorage[group]}", LogType.LOW)
	} else {
		log("duplicate ID $id for group registered $group: ${idstorage[group]}", LogType.WARNING)
		throw Exit("TODO")
	}
}

fun getFreeID(group: IDGroups): Long {
	val id: Long = idstorage[group]!!.run {
		sort()
		var idTmp: Long = 0
		forEach { i ->
			if(idTmp != i)
				return@run idTmp;
			idTmp++
		}
		return@run idTmp;
	}
	log("created id $id", LogType.IMPORTANT)
	usedID(group, id)
	return id
}