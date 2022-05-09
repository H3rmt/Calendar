package frame

import calendar.Appointment
import calendar.Note
import calendar.Timing
import calendar.Type
import javafx.collections.*
import logic.Configs
import logic.getConfig
import java.time.DayOfWeek
import java.time.LocalDate


interface CellDisplay {
	val notes: ObservableList<Note>
	
	val time: LocalDate
}

@Suppress("LongParameterList")
class Week(_time: LocalDate, Monday: Day, Tuesday: Day, Wednesday: Day, Thursday: Day, Friday: Day, Saturday: Day, Sunday: Day, val WeekOfYear: Int): CellDisplay {
	
	override val time: LocalDate = _time
	
	override val notes: ObservableList<Note> = FXCollections.observableArrayList()
	
	val appointments: List<Appointment>
		get() {
			val list = mutableListOf<Appointment>()
			for(day in allDays.values) {
				list.addAll(day.appointments)
			}
			return list
		}
	
	val allDays: Map<DayOfWeek, Day> = mapOf(
		DayOfWeek.MONDAY to Monday,
		DayOfWeek.TUESDAY to Tuesday,
		DayOfWeek.WEDNESDAY to Wednesday,
		DayOfWeek.THURSDAY to Thursday,
		DayOfWeek.FRIDAY to Friday,
		DayOfWeek.SATURDAY to Saturday,
		DayOfWeek.SUNDAY to Sunday
	)
	
	fun getAllAppointmentsSorted(): Map<Type, Int> {
		val list = mutableMapOf<Type, Int>()
		for(appointment in appointments) {
			list[appointment.type] = list[appointment.type]?.plus(1) ?: 1
		}
		return list
	}
	
	fun addAppointments(list: List<Appointment>) {
		val appointmentList = mutableMapOf<DayOfWeek, MutableList<Appointment>?>()
		list.forEach { appointmentList[Timing.fromUTCEpochMinuteToLocalDateTime(it.start).dayOfWeek]?.add(it) ?: listOf(it) }
		for((key, value) in appointmentList) {
			allDays[key]?.appointments?.addAll(value ?: listOf())
		}
	}
	
	override fun toString(): String = "$time $notes $allDays"
}


data class Day(override val time: LocalDate, val partOfMonth: Boolean): CellDisplay {
	
	var appointments: MutableList<Appointment> = mutableListOf()
	
	override var notes: ObservableList<Note> = FXCollections.observableArrayList()
	
	fun getAppointmentsLimited(): List<Appointment> = appointments.subList(0, minOf(appointments.size, getConfig<Double>(Configs.MaxDayAppointments).toInt()))
	
	override fun toString(): String = "$time $notes $appointments"
}