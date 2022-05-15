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
import java.time.DayOfWeek
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
	val title = text("title").default("")
	val description = text("description").default("")
	val week = bool("week").default(false)
	
	// week appointment start with 0 as Monday (so start 0-10.080, end between 1-20.160, if longer it reaches from end of week back to beginning )
	val allDay = bool("allDay").default(false)
	val type = reference("type", TypeTable)
}

object Appointments: DBObservableList<Appointment>(Appointment.Appointments) {
	
	/**
	 * returns an observable List containing Week Appointments that gets updated every time
	 * - the list of Notes change
	 * - time of any note changes and it (Note) fulfills the timespan condition
	 *
	 * listeners for any new Appointments added after creation of the list get added automatically
	 */
	fun getWeekAppointments(): ObservableList<Appointment> { // probably needed for configuration of week appointments
		val condition: (Appointment) -> Boolean = {
			it.week.value
		}
		
		// populate list
		val list = observableListOf<Appointment>(this.filter(condition))
		
		// adds or removes appointment from/to returned list based on condition
		val check: (Appointment) -> Unit = { appointment ->
			if(condition(appointment)) { // if appointment fulfills condition
				if(!list.contains(appointment))
					list.remove(appointment)  // remove not if on list
			} else { // if appointment doesn't fulfill condition
				if(list.contains(appointment))
					list.add(appointment)  // add appointment if not on list
			}
		}
		
		// add listener to every value of appointment that is evaluated in condition
		// to check again if appointment should be added or removed from returned list
		val addChecks: (Appointment) -> Unit = { appointment ->
			appointment.start.listen {
				check(appointment)
			}
			appointment.end.listen {
				check(appointment)
			}
			appointment.week.listen {
				check(appointment)
			}
		}
		
		// add checks to all current appointments in list
		forEach(addChecks)
		
		// runs if new appointment is created or removed
		listen2 { new, old ->
			val oldAppointments = old.toSet().minus(new) // filter out removed appointments
			list.removeAll(oldAppointments) // remove old appointments to returned list
			val newAppointments = new.toSet().minus(old) // filter out added appointments
			list.addAll(newAppointments)  // add new appointments to returned list

//			list.setAll(new) // triggers listeners only once, but clears whole list first TODO test this
			
			// add checks to all new appointments in list
			newAppointments.forEach(addChecks)
		}
		return list
		
	}
	
	/**
	 * returns an observable List containing non Week Appointments within timespan that gets updated every time
	 * - the list of Notes change
	 * - time of any note changes and it (Note) fulfills the timespan condition
	 *
	 * listeners for any new Appointments added after creation of the list get added automatically
	 */
	fun getAppointmentsFromTo(from: LocalDateTime, to: LocalDateTime, day: DayOfWeek): ObservableList<Appointment> {
		val condition: (Appointment) -> Boolean = {
			(!it.week.value && it.start.value >= from && it.end.value <= to) || (it.week.value && day in it.start.value.dayOfWeek..it.end.value.dayOfWeek)
		}
		
		// populate list
		val list = observableListOf<Appointment>(this.filter(condition))
		
		// adds or removes appointment from/to returned list based on condition
		val check: (Appointment) -> Unit = { appointment ->
			if(condition(appointment)) { // if appointment fulfills condition
				if(!list.contains(appointment))
					list.remove(appointment)  // remove not if on list
			} else { // if appointment doesn't fulfill condition
				if(list.contains(appointment))
					list.add(appointment)  // add appointment if not on list
			}
		}
		
		// add listener to every value of appointment that is evaluated in condition
		// to check again if appointment should be added or removed from returned list
		val addChecks: (Appointment) -> Unit = { appointment ->
			appointment.start.listen {
				check(appointment)
			}
			appointment.end.listen {
				check(appointment)
			}
			appointment.week.listen {
				check(appointment)
			}
		}
		
		// add checks to all current appointments in list
		forEach(addChecks)
		
		// runs if new appointment is created or removed
		listen2 { new, old ->
			val oldAppointments = old.toSet().minus(new) // filter out removed appointments
			list.removeAll(oldAppointments) // remove old appointments to returned list
			val newAppointments = new.toSet().minus(old) // filter out added appointments
			list.addAll(newAppointments)  // add new appointments to returned list

//			list.setAll(new) // triggers listeners only once, but clears whole list first TODO test this
			
			// add checks to all new appointments in list
			newAppointments.forEach(addChecks)
		}
		return list
	}
}

object NoteTable: LongIdTable() {
	val time = long("time")
	val text = text("text")
	val type = reference("type", TypeTable)
	val week = bool("week")
}

object Notes: DBObservableList<Note>(Note.Notes) {
	
	
	
	/**
	 * returns an observable List containing non Week Notes at this time that gets updated every time
	 * - the list of Notes change
	 * - time of any note changes and it (Note) fulfills the timespan condition
	 *
	 * listeners for any new Notes added after creation of the list get added automatically
	 */
	fun getNotesAt(at: LocalDate): ObservableList<Note> {
		val condition: (Note) -> Boolean = {
			!it.week.value && at == it.time.value
		}
		
		// populate list
		val list = observableListOf<Note>(filter(condition))
		
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
	
	/**
	 * returns an observable List containing Week Notes within timespan that gets updated every time
	 * - the list of Notes change
	 * - time of any note changes and it (Note) fulfills the timespan condition
	 *
	 * listeners for any new Notes added after creation of the list get added automatically
	 */
	fun getWeekNotesFromTo(from: LocalDate, to: LocalDate): ObservableList<Note> {
		
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

object Files: DBObservableList<File>(File.Files) {

}

object ReminderTable: LongIdTable() {
	val time = long("time") //.nullable() // not nullable, TODO clone appointment time and always use it (ask when editing appointment if reminder should be moved)
	val appointment = reference("appointment", AppointmentTable).nullable()
	val title = text("title")
	val description = text("description")
}

object Reminders: DBObservableList<Reminder>(Reminder.Reminders) {

}

object TypeTable: IntIdTable() {
	val name = text("name")
	val color = varchar("color", 20)
}

object Types: DBObservableList<Type>(Type.Types) {
	fun random(): Type = get().random()
}