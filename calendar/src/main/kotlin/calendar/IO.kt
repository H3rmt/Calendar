package calendar

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate
import java.time.LocalDateTime


fun initDb() {
	Database.connect("jdbc:sqlite:data/data.sqlite")
	// user = "myself", password = "secret")
	transaction {
		SchemaUtils.createMissingTablesAndColumns(AppointmentTable, FileTable, NoteTable, ReminderTable, TypeTable)
	}
}

fun getNotes(at: LocalDate): List<Note> {
	return transaction {
		return@transaction Note.Notes.all().filter {
			!it.week.value && at == it.time.value
		}
	}
}

fun getWeekNotes(from: LocalDate, to: LocalDate): List<Note> {
	return transaction {
		return@transaction Note.Notes.all().filter {
			it.week.value && from < it.time.value && it.time.value < to
		}
	}
}

fun getAppointments(): List<Appointment> {
	return transaction {
		return@transaction Appointment.Appointments.all().toList()
	}
}

fun getAppointments(from: LocalDateTime, to: LocalDateTime): List<Appointment> {
	return transaction {
		return@transaction Appointment.Appointments.all().filter {
			it.start.value >= from && it.end.value <= to
		}
	}
}

fun getWeekAppointments(): List<Appointment> {
	return transaction {
		return@transaction Appointment.Appointments.all().filter {
			it.week.value
		}
	}
}

fun getReminders(): List<Reminder> {
	return transaction {
		return@transaction Reminder.Reminders.all().toList()
	}
}

fun getTypes(): List<Type> {
	return transaction {
		return@transaction Type.Types.all().toList()
	}
}

object AppointmentTable: LongIdTable() {
	val start = long("start")
	val end = long("end")
	val title = text("title")
	val description = text("description")
	val week = bool("week")
	val allDay = bool("allDay").default(false)
	val type = reference("type", TypeTable)
}

object NoteTable: LongIdTable() {
	val time = long("time")
	val text = text("text")
	val type = reference("type", TypeTable)
	val week = bool("week")
}

object FileTable: LongIdTable() {
	val data = text("data")
	val name = text("text")
	val origin = text("origin")
	val note = reference("note", NoteTable)
}

object ReminderTable: LongIdTable() {
	val time = long("time").nullable()
	val appointment = reference("appointment", AppointmentTable).nullable()
	val title = text("title")
	val description = text("description")
}

object TypeTable: IntIdTable() {
	val name = text("name")
	val color = varchar("color", 20)
}
