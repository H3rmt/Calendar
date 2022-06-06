package calendar

import javafx.scene.paint.Color
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.transactions.transaction
import replaceNewline
import java.time.LocalDate
import java.time.LocalDateTime

class Appointment(id: EntityID<Long>): LongEntity(id), DBClass {
	object Appointments: LongEntityClass<Appointment>(AppointmentTable)
	
	companion object {
		fun new(
			start: LocalDateTime, end: LocalDateTime, title: String, description: String, type: Type,
			allDay: Boolean = false, week: Boolean = false
		): Appointment {
			return transaction {
				return@transaction Appointments.new {
					this.start.set(start)
					this.end.set(end)
					this.title.set(title)
					this.description.set(description)
					this.type.set(type)
					this.allDay.set(allDay)
					this.week.set(week)
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
	
	override fun remove() {
		calendar.Appointments.remove(this).also {
			transaction {
				delete()
			}
		}
	}
	
	// [{7} 2022-05-16T00:00 - 2022-05-16T23:59  [{1} test 0x008000ff] Day | test_1_title: test_1_desc]
	override fun toString(): String = ("[{${id.value}} ${start.value} - ${end.value}  ${type.value} " +
			  "${if(week.value) "Week" else "Day"} | " +
			  "${title.value}: ${description.value}]").replaceNewline()
	
	override fun equals(other: Any?): Boolean {
		return if(other !is Appointment) false
		else other.id == id
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


class Note(id: EntityID<Long>): LongEntity(id), DBClass {
	object Notes: LongEntityClass<Note>(NoteTable)
	
	companion object {
		fun new(time: LocalDate, text: String, type: Type, week: Boolean): Note {
			return transaction {
				return@transaction Notes.new {
					this.time.set(time)
					this.text.set(text)
					this.type.set(type)
					this.week.set(week)
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
	
	override fun remove() {
		calendar.Notes.remove(this).also {
			transaction {
				delete()
			}
		}
	}
	
	// [{2} 2022-05-16T00:00 [{1} test 0x008000ff] ||: test_note_text]
	override fun toString(): String =
		"[{${id.value}} ${time.value} ${type.value} |$files|: ${text.value}]".replaceNewline()
	
	override fun equals(other: Any?): Boolean {
		return if(other !is Note) false
		else other.id == id
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

class File(id: EntityID<Long>): LongEntity(id), DBClass {
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
	
	override fun remove() {
		calendar.Files.remove(this).also {
			transaction {
				delete()
			}
		}
	}
	
	// [{2} testfile C:/Users/fef/Documents/test.txt]
	override fun toString(): String = "[{${id.value}} ${name.value} ${origin.value}]".replaceNewline()
	
	override fun equals(other: Any?): Boolean {
		return if(other !is File) false
		else other.id == id
	}
	
	override fun hashCode(): Int {
		var result = name.hashCode()
		result = 31 * result + origin.hashCode()
		return result
	}
}

class Reminder(id: EntityID<Long>): LongEntity(id), DBClass {
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
	
	override fun remove() {
		calendar.Reminders.remove(this).also {
			transaction {
				delete()
			}
		}
	}
	
	// [{14} 2022-05-16T00:00 | test_title: test_description]
	override fun toString(): String =
		"[{${id.value}} ${time.value} | ${title.value}: ${description.value} (${appointment.value})]".replaceNewline()
	
	override fun equals(other: Any?): Boolean {
		return if(other !is Reminder) false
		else other.id == id
	}
	
	override fun hashCode(): Int {
		var result = time.hashCode()
		result = 31 * result + title.hashCode()
		result = 31 * result + description.hashCode()
		return result
	}
}


class Type(id: EntityID<Int>): IntEntity(id), DBClass {
	
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
	
	
	override fun remove() {
		calendar.Types.remove(this).also {
			transaction {
				delete()
			}
		}
	}
	
	// [{1} test_1_type 0x008000ff]
	override fun toString(): String = "[{${id.value}} ${name.value} ${color.value}]".replaceNewline()
	
	override fun equals(other: Any?): Boolean {
		return if(other !is Type) false
		else other.id == id
	}
	
	override fun hashCode(): Int {
		var result = name.hashCode()
		result = 31 * result + color.hashCode()
		return result
	}
}

interface DBClass {
	override fun hashCode(): Int
	override fun equals(other: Any?): Boolean
	override fun toString(): String
	fun remove()
}

