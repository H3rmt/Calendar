package calendar

import com.google.gson.annotations.Expose
import javafx.scene.paint.*
import logic.Configs
import logic.getConfig
import logic.getLangString
import java.time.ZonedDateTime
import kotlin.reflect.full.memberProperties


interface Celldisplay

class Week(
	_time: ZonedDateTime,
	Monday: Day,
	Tuesday: Day,
	Wednesday: Day,
	Thursday: Day,
	Friday: Day,
	Saturday: Day,
	Sunday: Day,
	val WeekofYear: Int
): Celldisplay {
	
	val time: ZonedDateTime = _time
	
	var general = this
	
	@Expose
	val alldays = arrayOf(Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday)
	
	fun getallappointments(): List<Appointment> {
		val list = mutableListOf<Appointment>()
		for(day in alldays) {
			list.addAll(day.appointments)
		}
		return list
	}
	
	fun getallappointmentssort(): Map<Types, List<Appointment>> {
		val list = mutableMapOf<Types, MutableList<Appointment>>()
		for(appointment in getallappointments()) {
			if(list[appointment.type] == null)
				list[appointment.type] = mutableListOf()
			list[appointment.type]!!.add(appointment)
		}
		return list
	}
	
	override fun toString(): String {
		return "${time.dayOfMonth} - ${time.plusDays(6).dayOfMonth} / ${getLangString(time.month.name)}"
	}
}

class Day(_time: ZonedDateTime, _partofmonth: Boolean): Celldisplay {
	
	val time: ZonedDateTime = _time
	
	@Expose
	val partofmonth: Boolean = _partofmonth
	
	@Expose
	val appointments: MutableList<Appointment> = mutableListOf()
	
	fun getappointmentslimit(): List<Appointment> {
		return appointments.subList(0, minOf(appointments.size, getConfig<Double>(Configs.MaxDayAppointments).toInt()))
	}
	
	override fun toString(): String {
		return "${time.dayOfMonth}:${time.dayOfWeek}"
	}
}

class Appointment(_day: String, _start: Long, _duration: Long, _title: String, _description: String, _type: Types) {
	
	@Expose
	// stored in minutes instead of milliseconds (60000 to 1)
	var day = _day
	
	@Expose
	var start = _start
	
	@Expose
	// stored in minutes instead of seconds (60 to 1)
	val duration = _duration
	
	@Expose
	val title = _title
	
	@Expose
	val description = _description
	
	@Expose
	val type = _type
	
	override fun toString(): String {
		var s = "Appointment{"
		this::class.memberProperties.forEach { s += it.name + ":" + it.getter.call(this) + " " }
		return "$s}"
	}
}

enum class Types {
	Work,
	Sport,
	School;
	
	fun getColor(): Paint {
		return when(this) {
			Work -> Color.BLUE
			Sport -> Color.BLACK
			School -> Color.RED
		}
	}
}