package calendar

import calendar.Timing.UTCEpochMinuteToLocalDateTime
import javafx.scene.paint.*
import logic.Configs
import logic.LogType
import logic.Warning
import logic.getConfig
import logic.getLangString
import logic.log
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.transactions.transaction
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
	
	fun addAppointments(list: List<Appointment>) {
		val appointmentlist = mutableMapOf<DayOfWeek, MutableList<Appointment>?>()
		list.forEach { appointmentlist[UTCEpochMinuteToLocalDateTime(it.start).dayOfWeek]?.add(it) ?: listOf(it) }
		for((key, value) in appointmentlist) {
			allDays[key]?.appointments?.addAll(value ?: listOf())
		}
	}
	
	override fun toString(): String = "$time $notes $allDays"
}


data class Day(override val time: LocalDate, val partOfMonth: Boolean): CellDisplay {
	
	var appointments: MutableList<Appointment> = mutableListOf()
	
	override var notes: MutableList<Note> = mutableListOf()
	
	fun getAppointmentsLimited(): List<Appointment> = appointments.subList(0, minOf(appointments.size, getConfig<Double>(Configs.MaxDayAppointments).toInt()))
	
	override fun toString(): String = "$time $notes $appointments"
}



class Appointment(id: EntityID<Long>): LongEntity(id) {
	
	object Appointments: LongEntityClass<Appointment>(AppointmentTable)
	
	companion object {
		fun new(_start: Long, _duration: Long, _title: String, _description: String, _type: Types, _week: Boolean): Appointment {
			return transaction {
				return@transaction Appointments.new {
					start = _start
					duration = _duration
					title = _title
					description = _description
					type = _type
					week = _week
				}
			}
		}
	}
	
	private var dbStart by AppointmentTable.start
	private var dbDuration by AppointmentTable.duration
	private var dbTitle by AppointmentTable.title
	private var dbDescription by AppointmentTable.description
	private var dbType by AppointmentTable.type
	private var dbWeek by AppointmentTable.week
	
	var start: Long
		get() = dbStart
		set(value) {
			transaction {
				dbStart = value
			}
		}
	
	var duration
		get() = dbDuration
		set(value) {
			transaction {
				dbDuration = value
			}
		}
	var title: String
		get() = dbTitle
		set(value) {
			transaction {
				dbTitle = value
			}
		}
	var description: String
		get() = dbDescription
		set(value) {
			transaction {
				dbDescription = value
			}
		}
	var type: Types
		get() = Types.valueOf(dbType)
		set(value) {
			transaction {
				dbType = value.toString()
			}
		}
	var week: Boolean
		get() = dbWeek
		set(value) {
			transaction {
				dbWeek = value
			}
		}
	
	fun remove() {
		transaction {
			delete()
		}
	}
	
	override fun toString(): String = "[${if(week) "Week" else "Day"}{$id} $start - $duration  $type | $title: $description]"
}


class Note(id: EntityID<Long>): LongEntity(id) {
	
	object Notes: LongEntityClass<Note>(NoteTable)
	
	companion object {
		fun new(_time: Long, _text: String, _type: Types, _week: Boolean, _file: File?): Note {
			return transaction {
				return@transaction Notes.new {
					time = _time
					text = _text
					type = _type
					week = _week
//					file = _file
				}
			}
		}
	}
	
	private var dbTime by NoteTable.time
	private var dbText by NoteTable.text
	private var dbType by NoteTable.type
	private var dbWeek by NoteTable.week
//	private var dbFile by File.Files referencedOn NoteTable.files
	
	var time: Long
		get() = dbTime
		set(value) {
			transaction {
				dbTime = value
			}
		}
	var text
		get() = dbText
		set(value) {
			transaction {
				dbText = value
			}
		}
	var type: Types
		get() = Types.valueOf(dbType)
		set(value) {
			transaction {
				dbType = value.toString()
			}
		}
	var week: Boolean
		get() = dbWeek
		set(value) {
			transaction {
				dbWeek = value
			}
		}
	
	//	var file: File
//		get() = dbFile
//		set(value) {
//			dbFile = value
//		}
	fun remove() {
		transaction {
			delete()
		}
	}
	
	//	override fun toString(): String = "[{$id} $time $type $file]"
	override fun toString(): String = "[{$id} $time $type]"
	
}

class File(id: EntityID<Long>): LongEntity(id) {
	
	object Files: LongEntityClass<File>(FileTable)
	
	companion object {
		fun new(_data: ByteArray, _name: String, _origin: String): File {
			return transaction {
				return@transaction Files.new {
					data = _data
					name = _name
					origin = _origin
				}
			}
		}
	}
	
	private var dbData by FileTable.data
	private var dbName by FileTable.name
	private var dbOrigin by FileTable.origin
	
	var data: ByteArray
		get() = dbData.encodeToByteArray()
		set(value) {
			transaction {
				dbData = value.decodeToString()
			}
		}
	var name: String
		get() = dbName
		set(value) {
			transaction {
				dbName = value
			}
		}
	var origin: String
		get() = dbOrigin
		set(value) {
			transaction {
				dbOrigin = value
			}
		}
	
	fun remove() {
		transaction {
			delete()
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