package calendar

import javafx.collections.*
import listen
import listen2
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import tornadofx.*
import java.time.LocalDate
import java.time.LocalDateTime


fun initDb() {
	Database.connect("jdbc:sqlite:data/data.sqlite")
	// user = "myself", password = "secret")
	transaction {
		SchemaUtils.createMissingTablesAndColumns(AppointmentTable, FileTable, NoteTable, ReminderTable, TypeTable)
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

val appointments = object: DBObservableList<Appointment>(Appointment.Appointments) {
	
	/**
	 * no need to listen on all appointments, as no appointment
	 * ever can get converted to a week appointment TODO check this
	 *
	 * all week appointments get created as week or not and added to this list
	 */
	fun getWeekAppointments(): ObservableList<Appointment> {
		val list = observableListOf<Appointment>()
		fun update(l: ObservableList<Appointment>) {
			list.setAll(l.filter { it.week.value })
		}
		update(this)
		listen { update(it) }
		return list
	}
	
	fun getAppointmentsFromTo(from: LocalDateTime, to: LocalDateTime): ObservableList<Appointment> {
		val list = observableListOf<Appointment>()
		fun update(l: ObservableList<Appointment>) {
			val l = l.filter { it.start.value >= from && it.end.value <= to }
			if(l != list)
				list.setAll()
		}
		
		val listener = ChangeListener<LocalDateTime> { _, _, _ -> update(this) }
		listen { // run if new appointment is created or removed
			update(it) // updates list if new appointment is created or removed
			forEach { app -> // add listender to every appointment and reloads observable list of start or end changes
				app.start.addListener(listener)
				app.end.addListener(listener)
			}
		}
		
		// populate list
		update(this)
		return list
	}
}

object NoteTable: LongIdTable() {
	val time = long("time")
	val text = text("text")
	val type = reference("type", TypeTable)
	val week = bool("week")
}

val notes = object: DBObservableList<Note>(Note.Notes) {
	fun getNotesAt(at: LocalDate): ObservableList<Note> {
		val list = observableListOf<Note>()
		fun update(l: ObservableList<Note>) {
			val l = l.filter { !it.week.value && at == it.time.value }
			if(l != list)
				list.setAll()
		}
		
		val listener = ChangeListener<LocalDate> { _, _, _ -> update(this) }
		listen { // run if new appointment is created or removed
			update(it) // updates list if new appointment is created or removed
			forEach { note -> // add listender to every appointment and reloads observable list of start or end changes
				note.time.addListener(listener)
			}
		}
		
		// populate list
		update(this)
		return list
	}
	
	/**
	 * returns an observable List containing Week Notes within timespan that gets updated every time
	 * - the list of Notes change
	 * - time of any note changes and it (Note) fulfills the timespan condition
	 *
	 * listeners for any new Notes added after creation of the list get added automatically
	 *
	 */
	fun getWeekNotesFromTo(from: LocalDate, to: LocalDate): List<Note> {
		
		// condition to check if note fulfills condition to be in returned list
		val condition: (Note) -> Boolean = {
			it.week.value && from < it.time.value && it.time.value < to
		}
		
		// populate list
		val list = observableListOf<Note>(this.filter(condition))
		
		// adds or removes note from/to returned list based on condition
		val check: (Note) -> Unit = { note: Note ->
			if(condition(note)) { // if note fulfills condition
				if(!list.contains(note))
					list.remove(note)  // remove not if on list
			} else { // if note doesn't fulfill condition
				if(list.contains(note))
					list.add(note)  // add note if not on list
			}
		}
		
		// add listener to every value of note that is evaluated in condition
		// to check again if note should be added or removed from returned list
		val addChecks: (Note) -> Unit = { note ->
			note.time.listen {
				check(note)
			}
			note.week.listen {
				check(note)
			}
		}
		
		// add checks to all current notes in list
		forEach(addChecks)
		
		// runs if new appointment is created or removed
		listen2 { new, old ->
			val oldNotes = old.toSet().minus(new) // filter out removed notes
			list.removeAll(oldNotes) // remove old notes to returned list
			val newNotes = new.toSet().minus(old) // filter out added notes
			list.addAll(newNotes)  // add new notes to returned list

//			list.setAll(new) // triggers listeners only once, but clears whole list first TODO test this
			
			// add checks to all new notes in list
			newNotes.forEach(addChecks)
		}
		return list
	}
}

object FileTable: LongIdTable() {
	val data = text("data")
	val name = text("text")
	val origin = text("origin")
	val note = reference("note", NoteTable)
}

val files = object: DBObservableList<File>(File.Files) {

}

object ReminderTable: LongIdTable() {
	val time = long("time").nullable()
	val appointment = reference("appointment", AppointmentTable).nullable()
	val title = text("title")
	val description = text("description")
}

val reminders = object: DBObservableList<Reminder>(Reminder.Reminders) {

}

object TypeTable: IntIdTable() {
	val name = text("name")
	val color = varchar("color", 20)
}

val types = object: DBObservableList<Type>(Type.Types) {
}