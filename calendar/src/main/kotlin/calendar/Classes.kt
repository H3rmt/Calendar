package calendar

import calendar.File.Files.referrersOn
import javafx.scene.paint.*
import logic.Configs
import logic.getConfig
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SizedIterable
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
import java.time.LocalDateTime
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

interface CellDisplay {
	val notes: MutableList<Note>
	
	val time: LocalDate
}

@Suppress("LongParameterList")
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
	
	fun getAllAppointmentsSorted(): Map<Type, Int> {
		val list = mutableMapOf<Type, Int>()
		for(appointment in appointments) {
			list[appointment.type.value] = list[appointment.type.value]?.plus(1) ?: 1
		}
		return list
	}
	
	fun addAppointments(list: List<Appointment>) {
		val appointmentList = mutableMapOf<DayOfWeek, MutableList<Appointment>?>()
		list.forEach { appointmentList[it.start.value.dayOfWeek]?.add(it) ?: listOf(it) }
		for((key, value) in appointmentList) {
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
		fun new(_start: LocalDateTime, _end: LocalDateTime, _title: String, _description: String, _type: Type, _allDay: Boolean = false, _week: Boolean = false): Appointment {
			return transaction {
				return@transaction Appointments.new {
					start.set(_start)
					end.set(_end)
					title.set(_title)
					description.set(_description)
					type.set(_type)
					allDay.set(_allDay)
					week.set(_week)
				}
			}
		}
	}
	
	private var dbStart by AppointmentTable.start
	private var dbEnd by AppointmentTable.end
	private var dbTitle by AppointmentTable.title
	private var dbDescription by AppointmentTable.description
	private var dbAllDay by AppointmentTable.allDay
	private var dbType by Type.Types referencedOn AppointmentTable.type
	private var dbWeek by AppointmentTable.week
	
	val start: DBDateTimeObservable = DBDateTimeObservable(dbStart)
	val end: DBDateTimeObservable = DBDateTimeObservable(dbEnd)
	val title: DBObservable<String> = DBObservable(dbTitle)
	val description: DBObservable<String> = DBObservable(dbDescription)
	val allDay: DBObservable<Boolean> = DBObservable(dbAllDay)
	val type: DBObservable<Type> = DBObservable(dbType)
	val week: DBObservable<Boolean> = DBObservable(dbWeek)
	
	fun remove() {
		transaction {
			delete()
		}
	}
	
	override fun toString(): String = "[${if(week.value) "Week" else "Day"}{$id} $start - $end  $type | $title: $description]"
	
	override fun equals(other: Any?): Boolean {
		return if(other !is Appointment) false
		else start == other.start && end == other.end && title == other.title && description == other.description && type == other.type && week == other.week
	}
	
	override fun hashCode(): Int {
		var result = start.hashCode()
		result = 31 * result + end.hashCode()
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
		fun new(_time: LocalDate, _text: String, _type: Type, _week: Boolean): Note {
			return transaction {
				return@transaction Notes.new {
					time.set(_time)
					text.set(_text)
					type.set(_type)
					week.set(_week)
				}
			}
		}
	}
	
	private var dbTime by NoteTable.time
	private var dbText by NoteTable.text
	private var dbType by Type.Types referencedOn NoteTable.type
	private var dbWeek by NoteTable.week
	private val dbFiles by File.Files referrersOn FileTable.note
	
	val time: DBDateObservable = DBDateObservable(dbTime)
	val text: DBObservable<String> = DBObservable(dbText)
	val type: DBObservable<Type> = DBObservable(dbType)
	val week: DBObservable<Boolean> = DBObservable(dbWeek)
	val files: DBObservable<SizedIterable<File>> = DBObservable(dbFiles)
	
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
//					data.set(_data)
					name.set(_name)
					origin.set(_origin)
				}
			}
		}
	}
	
//	private var dbData by FileTable.data
	private var dbName by FileTable.name
	private var dbOrigin by FileTable.origin
	
//	var data: DBObservableD<ByteArray, String> = object: DBObservableD<ByteArray, String>(dbData) {
//		override fun convertFrom(value: ByteArray?): String? = value?.decodeToString()
//		override fun convertTo(value: String?): ByteArray? = value?.encodeToByteArray()
//	}
	var name: DBObservable<String> = DBObservable(dbName)
	var origin: DBObservable<String> = DBObservable(dbOrigin)
	
	fun remove() {
		transaction {
			delete()
		}
	}
	
	override fun toString(): String = "[{$id} $name $origin]"
	
	override fun equals(other: Any?): Boolean {
		return if(other !is File) false
		else other.name == name && origin == other.origin
	}
	
	override fun hashCode(): Int {
		var result = name.hashCode()
		result = 31 * result + origin.hashCode()
		return result
	}
}

class Reminder(id: EntityID<Long>): LongEntity(id) {
	object Reminders: LongEntityClass<Reminder>(ReminderTable)
	
	companion object {
		fun new(_time: LocalDateTime, _appointment: Appointment?, _title: String, _description: String): Reminder {
			return transaction {
				return@transaction Reminders.new {
					time.set(_time)
					appointment.set(_appointment)
					title.set(_title)
					description.set(_description)
				}
			}
		}
	}
	
	private var dbTime by ReminderTable.time
	private var dbAppointment by Appointment.Appointments optionalReferencedOn ReminderTable.appointment
	private var dbTitle by ReminderTable.title
	private var dbDescription by ReminderTable.description
	
	val time: DBDateTimeObservable = DBDateTimeObservable(dbTime)
	val appointment: DBObservable<Appointment?> = DBObservable(dbAppointment)
	val title: DBObservable<String> = DBObservable(dbTitle)
	val description: DBObservable<String> = DBObservable(dbDescription)
	
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
					name.set(_name)
					color.set(_color)
				}
			}
		}
	}
	
	private var dbName by TypeTable.name
	private var dbColor by TypeTable.color
	
	val name: DBObservable<String> = DBObservable(dbName)
	val color: DBObservableD<Color, String> = object: DBObservableD<Color, String>(dbColor) {
		override fun convertFrom(value: Color?): String = value.toString()
		override fun convertTo(value: String?): Color? = Color.valueOf(value)
	}
	
	
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


