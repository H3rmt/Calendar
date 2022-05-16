import calendar.Appointment
import calendar.Note
import calendar.Type
import javafx.collections.*
import logic.Configs
import logic.getConfig
import java.time.DayOfWeek
import java.time.LocalDate

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
		DayOfWeek.MONDAY to days[0],
		DayOfWeek.TUESDAY to days[1],
		DayOfWeek.WEDNESDAY to days[2],
		DayOfWeek.THURSDAY to days[3],
		DayOfWeek.FRIDAY to days[4],
		DayOfWeek.SATURDAY to days[5],
		DayOfWeek.SUNDAY to days[6]
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
