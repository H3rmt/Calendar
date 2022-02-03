package calendar

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

/*
fun saveDayNote(note: Note, add: Boolean = true) {
	log("${if(add) "Adding" else "Removing"} Day Note $note", LogType.IMPORTANT)
	val noteTime = UTCEpochMinuteToLocalDateTime(note.time)
	
	File(ConfigFiles.notesDir + "/${noteTime.year}/${noteTime.month.name}.json").run {
		if(!exists()) {
			log("file with notes for ${noteTime.year}/${noteTime.month.name} not found", LogType.LOW)
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
	
	File(ConfigFiles.notesDir + "/${noteTime.year}/${noteTime.month.name}.json").run {
		val original =
			getJson().fromJson<Map<String, ArrayList<Map<String, Any>>>>(
				getJsonReader(FileReader(ConfigFiles.notesDir + "/${noteTime.year}/${noteTime.month.name}.json")),
				Map::class.java
			)
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
	
	File(ConfigFiles.notesDir + "/${noteTime.year}/${noteTime.month.name}.json").run {
		if(!exists()) {
			log("file with notes for ${noteTime.year}/${noteTime.month.name} not found", LogType.LOW)
			createNewFile()
			writeText("{\n\t\"Week Notes\": [],\n\t\"Day Notes\": []\n}")
		}
	}
	
	resetGroup(IDGroups.Notes)
	val tmpWeekNotes: MutableMap<Int, MutableList<Note>> = mutableMapOf()
	
	getJson().fromJson<Map<String, ArrayList<Map<String, Any>>>>(getJsonReader(FileReader(ConfigFiles.notesDir + "/${noteTime.year}/${noteTime.month.name}.json")), Map::class.java)
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
	
	File(ConfigFiles.notesDir + "/${noteTime.year}/${noteTime.month.name}.json").run {
		val original =
			getJson().fromJson<Map<String, ArrayList<Map<String, Any>>>>(
				getJsonReader(FileReader(ConfigFiles.notesDir + "/${noteTime.year}/${noteTime.month.name}.json")),
				Map::class.java
			)
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
	
	File(ConfigFiles.appointmentsDir + "/${appointmentTime.year}/${appointmentTime.month.name}.json").run {
		if(!exists()) {
			log("file with notes for ${appointmentTime.year}/${appointmentTime.month.name} not found", LogType.WARNING)
			createNewFile()
			writeText("[\n\t\n]")
		}
	}
	
	resetGroup(IDGroups.Appointments)
	val tmpDayAppointments: MutableMap<Int, MutableList<Appointment>> = mutableMapOf()
	
	getJson().fromJson<ArrayList<Map<String, Any>>>(
		getJsonReader(FileReader(ConfigFiles.appointmentsDir + "/${appointmentTime.year}/${appointmentTime.month.name}.json")),
		List::class.java
	)
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
	
	File(ConfigFiles.appointmentsDir + "/${appointmentTime.year}/${appointmentTime.month.name}.json").run {
		val list = mutableListOf<Map<String, Any>>()
		tmpDayAppointments.forEach { list.addAll(it.value.map { note -> note.toJSON() }) }
		
		writeText(getJson().toJson(list))
	}
}

val idStorage: MutableMap<IDGroups, MutableList<Long>> = mutableMapOf(
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
	idStorage[group]!!.clear()
}

fun usedID(group: IDGroups, id: Long) {
	if(!idStorage[group]!!.contains(id)) {
		idStorage[group]!!.add(id)
		log("added ID $id for group $group: ${idStorage[group]}", LogType.LOW)
	} else {
		log("duplicate ID $id for group registered $group: ${idStorage[group]}", LogType.WARNING)
		throw Exit("TODO")
	}
}

fun getFreeID(group: IDGroups): Long {
	val id: Long = idStorage[group]!!.run {
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
*/


fun initDb() {
	Database.connect("jdbc:sqlite:data/data.sqlite")
}

fun saveDayAppointmentDb(appointment: Appointment, add: Boolean = true) {
	transaction {
		SchemaUtils.createDatabase("calendar")
		SchemaUtils.create(AppointmentTable)
		
		if(add)
			AppointmentDtO.new {
				start = appointment.start
				duration = appointment.duration
				title = appointment.title
				description = appointment.description
				type = appointment.type.toString()
			}
		else
			AppointmentDtO[appointment.id].delete()
	}
}

fun saveDayNoteDb(note: Note, add: Boolean = true) {
	transaction {
		SchemaUtils.createDatabase("calendar")
		SchemaUtils.create(NoteTable)
		
		if(add)
			NoteDtO.new {
				time = note.time
				text = note.text
				week = false
				type = note.type.toString()
			}
		else
			NoteDtO[note.id].delete()
	}
}


fun saveWeekNoteDb(note: Note, add: Boolean = true) {
	transaction {
		SchemaUtils.createDatabase("calendar")
		SchemaUtils.create(NoteTable)
		
		if(add)
			NoteDtO.new {
				time = note.time
				text = note.text
				week = true
				type = note.type.toString()
			}
		else
			NoteDtO[note.id].delete()
	}
}

object AppointmentTable: LongIdTable() {
	val start = long("start")
	val duration = long("duration")
	val title = text("title")
	val description = text("description")
	val type = varchar("type", 20)
}

class AppointmentDtO(id: EntityID<Long>): LongEntity(id) {
	companion object: LongEntityClass<AppointmentDtO>(AppointmentTable)
	
	var start by AppointmentTable.start
	var duration by AppointmentTable.duration
	var title by AppointmentTable.title
	var description by AppointmentTable.description
	var type by AppointmentTable.type
}

object NoteTable: LongIdTable() {
	val time = long("time")
	val text = text("text")
	val type = varchar("type", 20)
	val week = bool("week")
	val files = reference("files", FileTable)
}


class NoteDtO(id: EntityID<Long>): LongEntity(id) {
	companion object: LongEntityClass<NoteDtO>(NoteTable)
	
	var time by NoteTable.time
	var text by NoteTable.text
	var type by NoteTable.type
	var week by NoteTable.week
	var files by FileDtO referencedOn NoteTable.files
}

object FileTable: LongIdTable() {
	val data = binary("data")
	val name = text("text")
	val origin = text("origin")
}


class FileDtO(id: EntityID<Long>): LongEntity(id) {
	companion object: LongEntityClass<FileDtO>(FileTable)
	
	var data: ByteArray by FileTable.data
	var name: String by FileTable.name
	var origin: String by FileTable.origin
}