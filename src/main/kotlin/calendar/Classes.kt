package calendar

import javafx.scene.paint.*
import logic.Configs
import logic.LogType
import logic.Warning
import logic.getConfig
import logic.getLangString
import logic.log
import java.time.DayOfWeek
import java.time.DayOfWeek.FRIDAY
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.SATURDAY
import java.time.DayOfWeek.SUNDAY
import java.time.DayOfWeek.THURSDAY
import java.time.DayOfWeek.TUESDAY
import java.time.DayOfWeek.WEDNESDAY
import java.time.LocalDate
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

// implemented in companion object to have static method to create object
interface CreateFromStorage {
	fun <T> fromJSON(map: Map<String, Any>): T?
}

interface Storage {
	val id: Long
	
	fun toJSON(): Map<String, Any>
}

interface CellDisplay {
	val notes: MutableList<Note>
	
	val time: LocalDate
}

class Week(_time: LocalDate, Monday: Day, Tuesday: Day, Wednesday: Day, Thursday: Day, Friday: Day, Saturday: Day, Sunday: Day, val WeekOfYear: Int): CellDisplay {
	
	override val time: LocalDate = _time
	
	override val notes: MutableList<Note> = mutableListOf()
	
	val appointments: List<Appointment>
		get() {
			val list = mutableListOf<Appointment>()
			for(day in allDays.values) {
				list.addAll(day.appointments)
			}
			return list
		}
	
	val allDays: Map<DayOfWeek, Day> = mapOf(
		MONDAY to Monday, TUESDAY to Tuesday, WEDNESDAY to Wednesday, THURSDAY to Thursday, FRIDAY to Friday, SATURDAY to Saturday, SUNDAY to Sunday
	)
	
	fun getallAppointmentsSorted(): Map<Types, List<Appointment>> {
		val list = mutableMapOf<Types, MutableList<Appointment>>()
		for(appointment in appointments) {
			if(list[appointment.type] == null) list[appointment.type] = mutableListOf()
			list[appointment.type]!!.add(appointment)
		}
		return list
	}
	
	val date: String
		get() = "${time.dayOfMonth} - ${time.plusDays(6).dayOfMonth} / ${getLangString(time.month.name)}"
	
	//fun toDate(): String = "${time.dayOfMonth} - ${time.plusDays(6).dayOfMonth} / ${getLangString(time.month.name)}"
	
	fun addAppointments(appointmentslist: Map<DayOfWeek, List<Appointment>>) {
		for((key, value) in appointmentslist) {
			allDays[key]?.appointments?.addAll(value)
		}
	}
	
	override fun toString(): String = "$time $notes $allDays"
}


data class Day(override val time: LocalDate, val partOfMonth: Boolean): CellDisplay {
	
	val appointments: MutableList<Appointment> = mutableListOf()
	
	override val notes: MutableList<Note> = mutableListOf()
	
	fun getAppointmentsLimited(): List<Appointment> = appointments.subList(0, minOf(appointments.size, getConfig<Double>(Configs.MaxDayAppointments).toInt()))
	
	override fun toString(): String = "$time $notes $appointments"
}


open class Appointment(
	val start: Long, val duration: Long, val title: String, val description: String, val type: Types, override val id: Long, val daystart: Int = Timing.startofDayMinutes(start)
): Storage {
	
	// create New
	constructor(start: Long, duration: Long, title: String, description: String, type: Types): this(
		start, duration, title, description, type, getFreeID(IDGroups.Appointments)
	)
	
	override fun toJSON(): Map<String, Any> {
		return mapOf(
			"id" to id.toDouble(), "start" to start.toDouble(), "duration" to duration.toDouble(), "type" to type.toString(), "title" to title, "description" to description
		)
	}
	
	companion object: CreateFromStorage {
		override fun <Appointment> fromJSON(map: Map<String, Any>): Appointment? {
			try {
				return Appointment(
					(map["start"] as Double).toLong(),
					(map["duration"] as Double).toLong(),
					map["title"] as String,
					map["description"] as String,
					Types.valueOf(map["type"] as String),
					(map["id"] as Double).toLong()
				) as Appointment
			} catch(e: Exception) {
				Warning("an35f7", e, "Exception creating Appointment from map:$map")
				return null
			}
		}
	}
	
	override fun toString(): String = "[{$id} $start - $duration  $type | $title: $description]"
}

class WeekAppointment(
	val day: DayOfWeek, start: Long, duration: Long, title: String, description: String, type: Types, id: Long
): Appointment(start, duration, title, description, type, id) {
	
	@Suppress("unused")
	constructor(day: DayOfWeek, start: Long, duration: Long, title: String, description: String, type: Types): this(
		day, start, duration, title, description, type, getFreeID(IDGroups.Appointments)
	)
	
	override fun toJSON(): Map<String, Any> {
		return mapOf(
			"id" to id.toDouble(),
			"day" to day.toString(),
			"start" to start.toDouble(),
			"duration" to duration.toDouble(),
			"type" to type.toString(),
			"title" to title,
			"description" to description
		)
	}
	
	companion object: CreateFromStorage {
		override fun <WeekAppointment> fromJSON(map: Map<String, Any>): WeekAppointment? {
			try {
				return WeekAppointment(
					DayOfWeek.valueOf((map["day"] as String).uppercase()),
					(map["start"] as Double).toLong(),
					(map["duration"] as Double).toLong(),
					map["title"] as String,
					map["description"] as String,
					Types.valueOf(map["type"] as String),
					(map["id"] as Double).toLong()
				) as WeekAppointment
			} catch(e: Exception) {
				Warning("an35f7", e, "Exception creating Appointment from map:$map")
				return null
			}
		}
	}
	
	override fun toString(): String = "[{$id} $day: $start - $duration  $type | $title: $description]"
}


data class Note(
	val time: Long, var text: String, val type: Types, val files: List<File>, override val id: Long, val daystart: Int = Timing.startofDayMinutes(time)
): Storage {
	constructor(time: Long, text: String, type: Types, file: List<File>): this(
		time, text, type, file, getFreeID(IDGroups.Notes)
	)
	
	override fun toJSON(): Map<String, Any> {
		val files = mutableListOf<Map<String, Any>>()
		for(t in this.files) t.toJSON().let { files.add(it) }
		
		return mapOf(
			"id" to id.toDouble(), "time" to time.toDouble(), "text" to text, "type" to type.toString(), "files" to files.toList()
		)
	}
	
	companion object: CreateFromStorage {
		override fun <Note> fromJSON(map: Map<String, Any>): Note? {
			try {
				val tmp = map["files"] as List<*>
				val files = mutableListOf<File>()
				for(t in tmp) {
					@Suppress("UNCHECKED_CAST")
					File.fromJSON<File>(t as Map<String, Any>)?.let { files.add(it) }
				}
				
				return Note(
					(map["time"] as Double).toLong(), map["text"] as String, Types.valueOf(map["type"] as String), files, (map["id"] as Double).toLong()
				) as Note
			} catch(e: Exception) {
				Warning("an35f7", e, "Exception creating Note from map:$map")
				return null
			}
		}
	}
	
	override fun toString(): String = "[{$id} $time $type $files]"
}

data class File(
	val data: ByteArray, val name: String, val origin: String, override val id: Long
): Storage {
	
	constructor(data: ByteArray, name: String, origin: String): this(
		data, name, origin, getFreeID(IDGroups.Files)
	)
	
	@Suppress("unused")
	constructor(file: java.io.File): this(file.inputStream().readAllBytes(), file.name, file.absolutePath)
	
	override fun toJSON(): Map<String, Any> {
		return mapOf(
			"id" to id.toDouble(), "data" to data.toList(), "name" to name, "origin" to origin
		)
	}
	
	companion object: CreateFromStorage {
		override fun <File> fromJSON(map: Map<String, Any>): File? {
			try {
				@Suppress("UNCHECKED_CAST") val f = File(
					(map["data"] as List<Byte>).toByteArray(), map["name"] as String, map["origin"] as String, (map["id"] as Double).toLong()
				)
				return f as File
			} catch(e: Exception) {
				Warning("an35f7", e, "Exception creating NoteFile from map:$map")
				return null
			}
		}
	}
	
	override fun toString(): String = "[{$id} $name ${data.size} $origin]"
}


data class Types(val name: String, val color: Color) {
	
	override fun toString(): String = name
	
	companion object {
		
		private val types: MutableList<Types> = mutableListOf()
		
		fun clonetypes() = types.toCollection(mutableListOf())
		
		fun valueOf(s: String): Types {
			types.forEach {
				if(it.name.equals(s, true)) return@valueOf it
			}
			throw IllegalArgumentException("$s not a valid Type of $types")
		}
		
		private fun createType(type: Map<String, String>): Types? {
			try {
				if(type.containsKey("name") && type.containsKey("color")) {
					return Types(type["name"]!!, Color.valueOf(type["color"]!!))
				}
			} catch(e: Exception) {
				Warning("o2wi35", e, "Exception creating Type from map:$type")
			}
			return null
		}
		
		fun createTypes(data: List<Map<String, String>>) {
			types.clear()
			data.forEach {
				createType(it)?.apply {
					types.add(this)
					log("added type $this", LogType.LOW)
				}
			}
			log("created types $types", LogType.NORMAL)
		}
	}
}