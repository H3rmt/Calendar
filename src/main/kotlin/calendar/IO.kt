package calendar

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction


fun initDb() {
	Database.connect("jdbc:sqlite:data/data.sqlite")
	// user = "myself", password = "secret")
	transaction {
		SchemaUtils.createMissingTablesAndColumns(AppointmentTable, FileTable, NoteTable, ReminderTable, TypeTable)
	}
}

fun getNotes(at: Long): List<Note> {
	return transaction {
		return@transaction Note.Notes.all().filter {
			!it.week && at == it.time
		}
	}
}

fun getWeekNotes(from: Long, to: Long): List<Note> {
	return transaction {
		return@transaction Note.Notes.all().filter {
			it.week && from < it.time && it.time < to
		}
	}
}

fun getAppointments(from: Long, to: Long): List<Appointment> {
	return transaction {
		return@transaction Appointment.Appointments.all().filter {
			from <= it.start + it.duration && to > it.start
		}
	}
}

fun getWeekAppointments(): List<Appointment> {
	return transaction {
		return@transaction Appointment.Appointments.all().filter {
			it.week
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
	val duration = long("duration")
	val title = text("title")
	val description = text("description")
	val week = bool("week")
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
	val time = long("time")
	val appointment = reference("appointment", AppointmentTable).nullable()
	val title = text("title")
	val description = text("description")
}

object TypeTable: IntIdTable() {
	val name = text("name")
	val color = varchar("color", 20)
}