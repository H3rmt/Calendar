package calendar

import calendar.Appointment.Companion.new
import calendar.File.Companion.new
import calendar.Note.Companion.new
import calendar.Reminder.Companion.new
import calendar.Timing.toUTCEpochMinute
import calendar.Type.Companion.new
import javafx.scene.paint.*
import logic.LogType
import logic.log
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.transactions.transaction
import replaceNewline
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Appointment, representing an Appointment in the DB
 *
 * @constructor never invoked directly
 * @property start start of the appointment, set with start.set(start)
 * @property end end of the appointment, set with end.set(end)
 * @property title title of this appointment, set with title.set(title)
 * @property description description of this appointment, set with
 *     description.set(description)
 * @property allDay sets this Appointment to go from 0:00 to 24:00, set
 *     with allDay.set(allDay)
 * @property type type of this appointment, set with type.set(type)
 * @property week if true this appointment gets repeated on every week, set
 *     with week.set(week)
 *
 * create new with Appointment.new(_start = day.value.atStartOfDay(), ...)
 *
 * update with appointment?.let { app -> app.start.set(start), ... }
 *
 * @see AppointmentTable
 * @see DBClass
 * @see DBObservableBase
 * @see new
 */
class Appointment(id: EntityID<Long>): LongEntity(id), DBClass {
	object Appointments: LongEntityClass<Appointment>(AppointmentTable)

	companion object {
		/**
		 * create new Appointment
		 *
		 * @param _start start of the appointment
		 * @param _end end of the appointment
		 * @param _title title of the appointment
		 * @param _description description of the appointment
		 * @param _type type of the appointment
		 * @param _allDay sets this Appointment to go from 0:00 to 24:00
		 * @param _week if true this appointment gets repeated on every week
		 * @see Appointment
		 */
		@Suppress("LongParameterList")
		fun new(
			_start: LocalDateTime, _end: LocalDateTime, _title: String, _description: String, _type: Type,
			_allDay: Boolean = false, _week: Boolean = false
		): Appointment {
			return transaction {
				return@transaction Appointments.new {
					start.set(_start)
					end.set(_end)
					title.set(_title)
					description.set(_description)
					type.set(_type)
					allDay.set(_allDay)
					week.set(_week)
				}.also { calendar.Appointments.add(it); log("Appointment $it created", LogType.IMPORTANT) }
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

	/**
	 * Remove this Appointment from DB and from appointments list
	 *
	 * @see calendar.Appointments
	 */
	override fun remove() {
		calendar.Appointments.remove(this)
		log("Appointment $this removed", LogType.IMPORTANT)
		transaction {
			delete()
		}
	}

	// [{7} 2022-05-16T00:00 - 2022-05-16T23:59  [{1} test 0x008000ff] frame.Day | test_1_title: test_1_desc]
	override fun toString(): String = ("[{${id.value}} ${start.value} - ${end.value}  ${type.value} " +
			"${if(week.value) "Week" else "frame.Day"} | " +
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

/**
 * Note, representing a Note in the DB
 *
 * @constructor never invoked directly
 * @property time time of the note, set with time.set(time)
 * @property text text of the note, set with text.set(text)
 * @property type type of this note, set with type.set(type)
 * @property files files of this note, not implemented
 *
 * create new with Note.new(_time = day.value.atStartOfDay(), ...)
 *
 * update with note?.let { note -> note.time.set(time), ... }
 *
 * @see NoteTable
 * @see DBClass
 * @see DBObservableBase
 * @see new
 */
class Note(id: EntityID<Long>): LongEntity(id), DBClass {
	object Notes: LongEntityClass<Note>(NoteTable)

	companion object {
		/**
		 * create new Note
		 *
		 * @param _time time of the note
		 * @param _text text of the note
		 * @param _type type of this note
		 * @param _week files of this note, not implemented
		 * @see Note
		 */
		fun new(_time: LocalDate, _text: String, _type: Type, _week: Boolean): Note {
			return transaction {
				return@transaction Notes.new {
					time.set(_time)
					text.set(_text)
					type.set(_type)
					week.set(_week)
				}.also { calendar.Notes.add(it); log("Note $it created", LogType.IMPORTANT) }
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

	/**
	 * Remove this Note from DB and from note list
	 *
	 * @see calendar.Notes
	 */
	override fun remove() {
		calendar.Notes.remove(this)
		log("Note $this removed", LogType.IMPORTANT)
		transaction {
			delete()
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

/**
 * File, representing a File in the DB, not implemented
 *
 * TODO not implemented
 *
 * @constructor never invoked directly
 * @property data data of the file, not implemented
 * @property name name of the file, set with name.set(name)
 * @property origin origin of this file, set with origin.set(origin)
 *
 * create new with File.new(_name = "file", ...)
 *
 * update with file?.let { file -> file.name.set(name), ... }
 *
 * @see FileTable
 * @see DBClass
 * @see DBObservableBase
 * @see new
 */
class File(id: EntityID<Long>): LongEntity(id), DBClass {
	object Files: LongEntityClass<File>(FileTable)

	companion object {
		/**
		 * create new File
		 *
		 * @param _data data of the note, not implemented
		 * @param _name name of the note
		 * @param _origin origin of this note
		 * @see File
		 */
		fun new(_data: ByteArray, _name: String, _origin: String): File {
			return transaction {
				return@transaction Files.new {
//					data.set(_data)
					name.set(_name)
					origin.set(_origin)
				}.also { calendar.Files.add(it); log("File $it created", LogType.IMPORTANT) }
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

	/**
	 * Remove this File from DB and from file list
	 *
	 * @see calendar.Files
	 */
	override fun remove() {
		calendar.Files.remove(this)
		log("File $this removed", LogType.IMPORTANT)
		transaction {
			delete()
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

/**
 * Reminder, representing a Reminder in the DB
 *
 * either deadline is set so some time, or appointment is set to an
 * appointment, or both are none and reminder has no deadline
 *
 * @constructor never invoked directly
 * @property deadline deadline of the reminder, set with
 *     deadline.set(deadline)
 * @property appointment appointment of the reminder, set with
 *     appointment.set(appointment)
 * @property title title of this reminder, set with title.set(title)
 * @property description description of this reminder, set with
 *     description.set(description)
 *
 * create new with Reminder.new(_deadline = null, ...)
 *
 * update with reminder?.let { reminder -> reminder.deadline.set(deadline),
 * ... }
 *
 * @see ReminderTable
 * @see DBClass
 * @see DBObservableBase
 * @see new
 */
class Reminder(id: EntityID<Long>): LongEntity(id), DBClass {
	object Reminders: LongEntityClass<Reminder>(ReminderTable)

	companion object {
		/**
		 * create new Reminder
		 *
		 * @param _deadline deadline of the reminder
		 * @param _appointment appointment of the reminder
		 * @param _title title of this reminder
		 * @param _description description of this reminder
		 * @see Reminder
		 */
		fun new(_deadline: LocalDateTime?, _appointment: Appointment?, _title: String, _description: String): Reminder {
			return transaction {
				return@transaction Reminders.new {
					deadline.set(_deadline)
					appointment.set(_appointment)
					title.set(_title)
					description.set(_description)
				}.also { calendar.Reminders.add(it); log("Reminder $it created", LogType.IMPORTANT) }
			}
		}
	}

	private var dbDeadline by ReminderTable.deadline
	private var dbAppointment by Appointment.Appointments optionalReferencedOn ReminderTable.appointment
	private var dbTitle by ReminderTable.title
	private var dbDescription by ReminderTable.description

	val deadline: DBObservableBase<LocalDateTime?, Long?> = object: DBObservableBase<LocalDateTime?, Long?>() {
		override fun abstractGet(): Long? = dbDeadline
		override fun abstractSet(dat: Long?) {
			dbDeadline = dat
		}

		override fun convertFrom(value: LocalDateTime?): Long? = value?.toUTCEpochMinute()
		override fun convertTo(value: Long?): LocalDateTime? = if(value != null) Timing.fromUTCEpochMinuteToLocalDateTime(value) else null
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

	/**
	 * Remove this Reminder from DB and from reminders list
	 *
	 * @see calendar.Reminders
	 */
	override fun remove() {
		calendar.Reminders.remove(this)
		log("Reminder $this removed", LogType.IMPORTANT)
		transaction {
			delete()
		}
	}

	// [{14} 2022-05-16T00:00 | test_title: test_description]
	override fun toString(): String =
		"[{${id.value}} ${deadline.value} | ${title.value}: ${description.value} (${appointment.value})]".replaceNewline()

	override fun equals(other: Any?): Boolean {
		return if(other !is Reminder) false
		else other.id == id
	}

	override fun hashCode(): Int {
		var result = deadline.hashCode()
		result = 31 * result + title.hashCode()
		result = 31 * result + description.hashCode()
		return result
	}
}

/**
 * Type, representing a Type in the DB
 *
 * @constructor never invoked directly
 * @property name deadline of the type, set with name.set(name)
 * @property color appointment of the type, set with color.set(color)
 *
 * create new with Type.new(_name = "name", ...)
 *
 * update with type?.let { type -> type.deadline.set(deadline), ... }
 *
 * @see TypeTable
 * @see DBClass
 * @see DBObservableBase
 * @see new
 */
class Type(id: EntityID<Int>): IntEntity(id), DBClass {

	object Types: IntEntityClass<Type>(TypeTable)

	companion object {
		/**
		 * create a new Type in DB
		 *
		 * @param _name name of type
		 * @param _color color of type
		 * @see Type
		 */
		fun new(_name: String, _color: Color): Type {
			return transaction {
				return@transaction Types.new {
					name.set(_name)
					color.set(_color)
				}.also { calendar.Types.add(it); log("Type $it created", LogType.IMPORTANT) }
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

	/**
	 * Remove this Type from DB and from type list
	 *
	 * @see calendar.Types
	 */
	override fun remove() {
		calendar.Types.remove(this)
		log("Type $this removed", LogType.IMPORTANT)
		transaction {
			delete()
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

/** Superclass for all DataBase Classes */
interface DBClass {
	override fun hashCode(): Int
	override fun equals(other: Any?): Boolean
	override fun toString(): String
	fun remove()
}

