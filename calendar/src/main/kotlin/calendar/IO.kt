package calendar

import javafx.collections.*
import logic.Files
import logic.Language
import logic.LogType
import logic.listen
import logic.listenUpdates
import logic.log
import logic.translate
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import tornadofx.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.random.Random


fun initDb() {
	Database.connect("jdbc:sqlite:${Files.DBfile}")
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
		log("(Appointments) getting getWeekAppointments", LogType.LOW)
		// condition to check if appointment fulfills condition to be in returned list
		val condition: (Appointment) -> Boolean = {
			it.week.value
		}

		// populate list
		val list = observableListOf<Appointment>(this.filter(condition))

		// adds or removes appointment from/to returned list based on condition
		// either gets called when any relevant value on appointment changes or new appointments get added
		val check = { appointment: Appointment ->
			if(condition(appointment)) {
				if(!list.contains(appointment)) list.add(appointment)  // add appointment if not on list
			} else {
				if(list.contains(appointment)) list.remove(appointment)  // remove appointment if on list
			}
		}

		// add listener to every value of appointment that is evaluated in condition
		// to check again if appointment should be added or removed from returned list
		val addChangeChecks: (Appointment) -> Unit = { appointment ->
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
		forEach(addChangeChecks)

		// runs if new appointments were created or removed
		listenUpdates { event ->
			while(event.next()) {
				list.removeAll(event.removed) // remove all removed appointments
				list.addAll(event.addedSubList.filter(condition)) // add new appointments if they fulfill condition

				// add checks to all new appointments in list
				event.addedSubList.forEach(addChangeChecks)
			}
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
		log("(Appointments) getting AppointmentsFromTo $from - $to", LogType.LOW)
		// condition to check if appointment fulfills condition to be in returned list
		val condition: (Appointment) -> Boolean = { // TODO Week appointments
			!it.week.value && (it.start.value <= to && it.end.value >= from)//(it.week.value && day in it.start.value.dayOfWeek..it.end.value.dayOfWeek) // TODO this doesn't seem right (week appointments)
		}

		// populate list
		val list = observableListOf<Appointment>(this.filter(condition))

		// adds or removes appointment from/to returned list based on condition
		// either gets called when any relevant value on appointment changes or new appointments get added
		val check = { appointment: Appointment ->
			if(condition(appointment)) {
				if(!list.contains(appointment))
					list.add(appointment)  // add appointment if not on list
			} else {
				if(list.contains(appointment))
					list.remove(appointment)  // remove appointment if on list
			}
		}

		// add listener to every value of appointment that is evaluated in condition
		// to check again if appointment should be added or removed from returned list
		val addChangeChecks: (Appointment) -> Unit = { appointment ->
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
		forEach(addChangeChecks)

		// runs if new appointments were created or removed
		listenUpdates { event ->
			while(event.next()) {
				list.removeAll(event.removed) // remove all removed appointments
				list.addAll(event.addedSubList.filter(condition)) // add new appointments if they fulfill condition

				// add checks to all new appointments in list
				event.addedSubList.forEach(addChangeChecks)
			}
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
		log("(Notes) getting NotesAt at $at", LogType.LOW)
		// condition to check if note fulfills condition to be in returned list
		val condition: (Note) -> Boolean = {
			!it.week.value && at == it.time.value
		}

		// populate list
		val list = observableListOf<Note>(this.filter(condition))

		// adds or removes notes from/to returned list based on condition
		// either gets called when any relevant value on note changes or new notes get added
		val check = { note: Note ->
			if(condition(note)) {
				if(!list.contains(note)) list.add(note)  // add note if not on list
			} else {
				if(list.contains(note)) list.remove(note)  // remove note if on list
			}
		}

		// add listener to every value of note that is evaluated in condition
		// to check again if note should be added or removed from returned list
		val addChangeChecks: (Note) -> Unit = { note ->
			note.time.listen {
				check(note)
			}
			note.week.listen {
				check(note)
			}
		}

		// add checks to all current notes in list
		forEach(addChangeChecks)

		// runs if new notes were created or removed
		listenUpdates { event ->
			while(event.next()) {
				list.removeAll(event.removed) // remove all removed notes
				list.addAll(event.addedSubList.filter(condition)) // add new notes if they fulfill condition

				// add checks to all new notes in list
				event.addedSubList.forEach(addChangeChecks)
			}
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
		log("(Notes) getting WeekNotesFromTo $from - $to", LogType.LOW)
		// condition to check if note fulfills condition to be in returned list
		val condition: (Note) -> Boolean = {
			it.week.value && from < it.time.value && it.time.value < to
		}

		// populate list
		val list = observableListOf<Note>(this.filter(condition))

		// adds or removes notes from/to returned list based on condition
		// either gets called when any relevant value on note changes or new notes get added
		val check = { note: Note ->
			if(condition(note)) {
				if(!list.contains(note)) list.add(note)  // add note if not on list
			} else {
				if(list.contains(note)) list.remove(note)  // remove note if on list
			}
		}

		// add listener to every value of note that is evaluated in condition
		// to check again if note should be added or removed from returned list
		val addChangeChecks: (Note) -> Unit = { note ->
			note.time.listen {
				check(note)
			}
			note.week.listen {
				check(note)
			}
		}

		// add checks to all current notes in list
		forEach(addChangeChecks)

		// runs if new notes were created or removed
		listenUpdates { event ->
			while(event.next()) {
				list.removeAll(event.removed) // remove all removed notes
				list.addAll(event.addedSubList.filter(condition)) // add new notes if they fulfill condition

				// add checks to all new notes in list
				event.addedSubList.forEach(addChangeChecks)
			}
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

@Suppress("EmptyClassBlock")
object Files: DBObservableList<File>(File.Files)

object ReminderTable: LongIdTable() {
	val deadline = long("deadline").nullable()
	val appointment = reference("appointment", AppointmentTable).nullable()
	val title = text("title")
	val description = text("description")
}

@Suppress("EmptyClassBlock")
object Reminders: DBObservableList<Reminder>(Reminder.Reminders)

object TypeTable: IntIdTable() {
	val name = text("name")
	val color = varchar("color", 20)
}

object Types: DBObservableList<Type>(Type.Types) {
	fun getRandom(exceptionName: String): Type {
		log("(Types) getting Random", LogType.LOW)
		require(isNotEmpty()) { throw NoTypeFound(exceptionName) }
		return get(Random.nextInt(size))
	}
}

class NoTypeFound(name: String):
	Exception("no types for %s found (add some types in settings)".translate(Language.TranslationTypes.Global, name))
