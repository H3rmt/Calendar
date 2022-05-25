package calendar

import javafx.collections.*
import javafx.scene.paint.*
import logic.Configs
import logic.getConfig
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
import java.time.LocalDateTime
import kotlin.collections.set

interface CellDisplay {
	val notes: MutableList<Note>
	
	val time: LocalDate
}

class Week(override val time: LocalDate, days: List<Day>, val WeekOfYear: Int, override val notes: ObservableList<Note>): CellDisplay {
	
	val appointments: List<Appointment>
		get() {
			val list = mutableListOf<Appointment>()
			for(day in allDays.values) {
				list.addAll(day.appointments)
			}
			return list
		}
	
	val allDays: Map<DayOfWeek, Day> = mapOf(
		MONDAY to days[0], TUESDAY to days[1], WEDNESDAY to days[2], THURSDAY to days[3], FRIDAY to days[4], SATURDAY to days[5], SUNDAY to days[6]
	)
	
	fun getAllAppointmentsSorted(): Map<Type, Int> {
		val list = mutableMapOf<Type, Int>()
		for(appointment in appointments) {
			list[appointment.type.value] = list[appointment.type.value]?.plus(1) ?: 1
		}
		return list
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
				}.also { calendar.Appointments.add(it) }
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
	
	val start: DBDateTimeObservable = object: DBDateTimeObservable() {
		override fun abstractGet(): Long = dbStart
		override fun abstractSet(dat: Long) {
			dbStart = dat
		}
	}
	val end: DBDateTimeObservable = object: DBDateTimeObservable() {
		override fun abstractGet(): Long = dbEnd
		override fun abstractSet(dat: Long) {
			dbEnd = dat
		}
	}
	val title: DBObservable<String> = object: DBObservable<String>() {
		override fun abstractGet(): String = dbTitle
		override fun abstractSet(dat: String) {
			dbTitle = dat
		}
	}
	val description: DBObservable<String> = object: DBObservable<String>() {
		override fun abstractGet(): String = dbDescription
		override fun abstractSet(dat: String) {
			dbDescription = dat
		}
	}
	val allDay: DBObservable<Boolean> = object: DBObservable<Boolean>() {
		override fun abstractGet(): Boolean = dbAllDay
		override fun abstractSet(dat: Boolean) {
			dbAllDay = dat
		}
	}
	val type: DBObservable<Type> = object: DBObservable<Type>() {
		override fun abstractGet(): Type = dbType
		override fun abstractSet(dat: Type) {
			dbType = dat
		}
	}
	val week: DBObservable<Boolean> = object: DBObservable<Boolean>() {
		override fun abstractGet(): Boolean = dbWeek
		override fun abstractSet(dat: Boolean) {
			dbWeek = dat
		}
	}
	
	fun remove() {
		transaction {
			delete()
		}.also { calendar.Appointments.remove(this) }
	}
	
	override fun toString(): String = "[{${id.value}} ${start.value} - ${end.value}  ${type.value} ${if(week.value) "Week" else "Day"} | ${title.value}: ${description.value}]"
	
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
				}.also { calendar.Notes.add(it) }
			}
		}
	}
	
	private var dbTime by NoteTable.time
	private var dbText by NoteTable.text
	private var dbType by Type.Types referencedOn NoteTable.type
	private var dbWeek by NoteTable.week
//	private val dbFiles by File.Files referrersOn FileTable.note
	
	val time: DBDateObservable = object: DBDateObservable() {
		override fun abstractGet(): Long = dbTime
		override fun abstractSet(dat: Long) {
			dbTime = dat
		}
	}
	val text: DBObservable<String> = object: DBObservable<String>() {
		override fun abstractGet(): String = dbText
		override fun abstractSet(dat: String) {
			dbText = dat
		}
	}
	val type: DBObservable<Type> = object: DBObservable<Type>() {
		override fun abstractGet(): Type = dbType
		override fun abstractSet(dat: Type) {
			dbType = dat
		}
	}
	val files = arrayListOf<File>() // TODO not implemented
	
	//	val files: DBObservable<SizedIterable<File>> = object: DBObservable<SizedIterable<File>>() {
//		override fun abstractGet(): SizedIterable<File> = dbFiles
//		override fun abstractSet(dat: SizedIterable<File>) {
//			TODO("TODO Not implemented")
//		}
//	}
	val week: DBObservable<Boolean> = object: DBObservable<Boolean>() {
		override fun abstractGet(): Boolean = dbWeek
		override fun abstractSet(dat: Boolean) {
			dbWeek = dat
		}
	}
	
	fun remove() {
		transaction {
			delete()
		}.also { calendar.Notes.remove(this) }
	}
	
	override fun toString(): String = "[{${id.value}} ${time.value} ${type.value} |$files| ]"
	
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
				}.also { calendar.Files.add(it) }
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
	var name: DBObservable<String> = object: DBObservable<String>() {
		override fun abstractGet(): String = dbName
		override fun abstractSet(dat: String) {
			dbName = dat
		}
	}
	var origin: DBObservable<String> = object: DBObservable<String>() {
		override fun abstractGet(): String = dbOrigin
		override fun abstractSet(dat: String) {
			dbOrigin = dat
		}
	}
	
	fun remove() {
		transaction {
			delete()
		}.also { calendar.Files.remove(this) }
	}
	
	override fun toString(): String = "[{${id.value}} ${name.value} ${origin.value}]"
	
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
				}.also { calendar.Reminders.add(it) }
			}
		}
	}
	
	private var dbTime by ReminderTable.time
	private var dbAppointment by Appointment.Appointments optionalReferencedOn ReminderTable.appointment
	private var dbTitle by ReminderTable.title
	private var dbDescription by ReminderTable.description
	
	val time: DBDateTimeObservable = object: DBDateTimeObservable() {
		override fun abstractGet(): Long = dbTime
		override fun abstractSet(dat: Long) {
			dbTime = dat
		}
	}
	val appointment: DBObservable<Appointment?> = object: DBObservable<Appointment?>() {
		override fun abstractGet(): Appointment? = dbAppointment
		override fun abstractSet(dat: Appointment?) {
			dbAppointment = dat
		}
	}
	val title: DBObservable<String> = object: DBObservable<String>() {
		override fun abstractGet(): String = dbTitle
		override fun abstractSet(dat: String) {
			dbTitle = dat
		}
	}
	val description: DBObservable<String> = object: DBObservable<String>() {
		override fun abstractGet(): String = dbDescription
		override fun abstractSet(dat: String) {
			dbDescription = dat
		}
	}
	
	fun remove() {
		transaction {
			delete()
		}.also { calendar.Reminders.remove(this) }
	}
	
	override fun toString(): String = "[{${id.value}} ${time.value} | ${title.value}: ${description.value}]"
	
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
				}.also { calendar.Types.add(it) }
			}
		}
	}
	
	private var dbName by TypeTable.name
	private var dbColor by TypeTable.color
	
	val name: DBObservable<String> = object: DBObservable<String>() {
		override fun abstractGet(): String = dbName
		
		override fun abstractSet(dat: String) {
			dbName = dat
		}
		
	}
	val color: DBObservableBase<Color, String> = object: DBObservableBase<Color, String>() {
		override fun convertFrom(value: Color): String = value.toString()
		override fun convertTo(value: String): Color = Color.valueOf(value)
		override fun abstractGet(): String = dbColor
		override fun abstractSet(dat: String) {
			dbColor = dat
		}
	}
	
	
	fun remove() {
		transaction {
			delete()
		}.also { calendar.Types.remove(this) }
	}
	
	override fun toString(): String = "[{${id.value}} ${name.value} ${color.value}]"
	
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


