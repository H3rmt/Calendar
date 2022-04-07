package calendar

import calendar.File.Files.referrersOn
import calendar.Timing.UTCEpochMinuteToLocalDateTime
import javafx.scene.paint.*
import logic.Configs
import logic.getConfig
import logic.getLangString
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
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
	
	fun getallAppointmentsSorted(): Map<Type, Int> {
		val list = mutableMapOf<Type, Int>()
		for(appointment in appointments) {
			list[appointment.type] = list[appointment.type]?.plus(1) ?: 1
		}
		return list
	}
	
	val date: String
		get() = "${time.dayOfMonth} - ${time.plusDays(6).dayOfMonth} / ${getLangString(time.month.name)}"

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
		fun new(_start: Long, _duration: Long, _title: String, _description: String, _type: Type, _week: Boolean): Appointment {
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
	private var dbType by Type.Types referencedOn AppointmentTable.type
	private var dbWeek by AppointmentTable.week
	
	var start: Long
		get() = transaction { dbStart }
		set(value) = transaction { dbStart = value }
	var duration
		get() = transaction { dbDuration }
		set(value) = transaction { dbDuration = value }
	var title: String
		get() = transaction { dbTitle }
		set(value) = transaction { dbTitle = value }
	var description: String
		get() = transaction { dbDescription }
		set(value) = transaction { dbDescription = value }
	var type: Type
		get() = transaction { dbType }
		set(value) = transaction { dbType = value }
	var week: Boolean
		get() = transaction { dbWeek }
		set(value) = transaction { dbWeek = value }
	
	fun remove() {
		transaction {
			delete()
		}
	}
	
	override fun toString(): String = "[${if(week) "Week" else "Day"}{$id} $start - $duration  $type | $title: $description]"
	
	override fun equals(other: Any?): Boolean {
		return if(other !is Appointment) false
		else start == other.start && duration == other.duration && title == other.title && description == other.description && type == other.type && week == other.week
	}
	
	override fun hashCode(): Int {
		var result = start.hashCode()
		result = 31 * result + duration.hashCode()
		result = 31 * result + title.hashCode()
		result = 31 * result + description.hashCode()
		result = 31 * result + type.hashCode()
		result = 31 * result + week.hashCode()
		return result
	}
}


class Note(id: EntityID<Long>): LongEntity(id) {
	
	object Notes: LongEntityClass<Note>(NoteTable)
	
	companion object {
		fun new(_time: Long, _text: String, _type: Type, _week: Boolean): Note {
			return transaction {
				return@transaction Notes.new {
					time = _time
					text = _text
					type = _type
					week = _week
				}
			}
		}
	}
	
	private var dbTime by NoteTable.time
	private var dbText by NoteTable.text
	private var dbType by Type.Types referencedOn NoteTable.type
	private var dbWeek by NoteTable.week
	private val dbFiles by File.Files referrersOn FileTable.note
	
	var time: Long
		get() = transaction { dbTime }
		set(value) = transaction { dbTime = value }
	var text: String
		get() = transaction { dbText }
		set(value) = transaction { dbText = value }
	var type: Type
		get() = transaction { dbType }
		set(value) = transaction { dbType = value }
	var week: Boolean
		get() = transaction { dbWeek }
		set(value) = transaction { dbWeek = value }
	val files: List<File>
		get() = transaction { dbFiles.toList() }
	
	fun remove() {
		transaction {
			delete()
		}
	}
	
	override fun toString(): String = "[{$id} $time $type |$files| ]"
	
	override fun equals(other: Any?): Boolean {
		return if(other !is Note) false
		else other.time == time && text == other.text && type == other.type && week == other.week && files == other.files
	}
	
	override fun hashCode(): Int {
		var result = time.hashCode()
		result = 31 * result + text.hashCode()
		result = 31 * result + type.hashCode()
		result = 31 * result + week.hashCode()
		result = 31 * result + files.hashCode()
		return result
	}
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
		get() = transaction { dbData.encodeToByteArray() }
		set(value) = transaction { dbData = value.decodeToString() }
	var name: String
		get() = transaction { dbName }
		set(value) = transaction { dbName = value }
	var origin: String
		get() = transaction { dbOrigin }
		set(value) = transaction { dbOrigin = value }
	
	fun remove() {
		transaction {
			delete()
		}
	}
	
	override fun toString(): String = "[{$id} $name ${data.size} $origin]"
	
	override fun equals(other: Any?): Boolean {
		return if(other !is File) false
		else other.name == name && data.contentEquals(other.data) && origin == other.origin
	}
	
	override fun hashCode(): Int {
		var result = data.contentHashCode()
		result = 31 * result + name.hashCode()
		result = 31 * result + origin.hashCode()
		return result
	}
}

class Reminder(id: EntityID<Long>): LongEntity(id) {
	
	object Reminders: LongEntityClass<Reminder>(ReminderTable)
	
	companion object {
		fun new(_time: Long, _title: String, _description: String): Reminder {
			return transaction {
				return@transaction Reminders.new {
					time = _time
					title = _title
					description = _description
				}
			}
		}
	}
	
	private var dbTime by ReminderTable.time
	private var dbTitle by ReminderTable.title
	private var dbDescription by ReminderTable.description
	
	var time: Long
		get() = transaction { dbTime }
		set(value) = transaction { dbTime = value }
	var title: String
		get() = transaction { dbTitle }
		set(value) = transaction { dbTitle = value }
	var description: String
		get() = transaction { dbDescription }
		set(value) = transaction { dbDescription = value }
	
	fun remove() {
		transaction {
			delete()
		}
	}
	
	override fun toString(): String = "[{$id} $time | $title: $description]"
	
	override fun equals(other: Any?): Boolean {
		return if(other !is Reminder) false
		else time == other.time && title == other.title && description == other.description
	}
	
	override fun hashCode(): Int {
		var result = time.hashCode()
		result = 31 * result + title.hashCode()
		result = 31 * result + description.hashCode()
		return result
	}
}


class Type(id: EntityID<Int>): IntEntity(id) {
	
	object Types: IntEntityClass<Type>(TypeTable)
	
	companion object {
		fun new(_name: String, _color: Color): Type {
			return transaction {
				return@transaction Types.new {
					name = _name
					color = _color
				}
			}
		}
	}
	
	private var dbName by TypeTable.name
	private var dbColor by TypeTable.color
	
	var name: String
		get() = transaction { dbName }
		set(value) = transaction { dbName = value }
	var color: Color
		get() = transaction { Color.valueOf(dbColor) }
		set(value) = transaction { dbColor = value.toString() }
	
	
	fun remove() {
		transaction {
			delete()
		}
	}
	
	override fun toString(): String = "[{$id} $name $color]"
	
	override fun equals(other: Any?): Boolean {
		return if(other !is Type) false
		else other.name == name && other.color == color
	}
	
	override fun hashCode(): Int {
		var result = name.hashCode()
		result = 31 * result + color.hashCode()
		return result
	}
}


