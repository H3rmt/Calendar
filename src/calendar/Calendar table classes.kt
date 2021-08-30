package calendar

import com.google.gson.annotations.Expose
import javafx.scene.paint.*
import logic.Configs
import logic.Warning
import logic.getConfig
import logic.getLangString
import java.time.DayOfWeek
import java.time.DayOfWeek.FRIDAY
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.SATURDAY
import java.time.DayOfWeek.SUNDAY
import java.time.DayOfWeek.THURSDAY
import java.time.DayOfWeek.TUESDAY
import java.time.DayOfWeek.WEDNESDAY
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
	
	private val time: ZonedDateTime = _time
	
	var general = this
	
	@Expose
	val alldays: Map<DayOfWeek, Day> = mapOf(
		MONDAY to Monday,
		TUESDAY to Tuesday,
		WEDNESDAY to Wednesday,
		THURSDAY to Thursday,
		FRIDAY to Friday,
		SATURDAY to Saturday,
		SUNDAY to Sunday
	)
	
	private fun getallappointments(): List<Appointment> {
		val list = mutableListOf<Appointment>()
		for(day in alldays.values) {
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
	
	fun addAppointments(appointmentslist: Map<DayOfWeek, List<Appointment>>) {
		for(day in appointmentslist) {
			alldays[day.key]?.appointments?.addAll(day.value)
		}
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
	val day = _day
	
	@Expose
	// stored in minutes instead of milliseconds (60 to 1)
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
		var s = "${this::class.simpleName}{"
		this::class.memberProperties.forEach { s += it.name + ":" + it.getter.call(this) + " " }
		return "$s}"
	}
}

class Types(_name: String, _color: Color) {
	
	val name: String = _name
	val color: Color = _color
	
	override fun toString(): String {
		var s = "${this::class.simpleName}{"
		this::class.memberProperties.forEach { s += it.name + ":" + it.getter.call(this) + " " }
		return "$s}"
	}
	
	companion object {
		private var types: MutableList<Types> = mutableListOf()
		
		fun getTypes(): List<Types> {
			return types.toCollection(mutableListOf())
		}
		
		fun valueOf(s: String): Types {
			types.forEach {
				if(it.name.equals(s, true))
					return@valueOf it
			}
			throw IllegalArgumentException("$s not a valid Type of $types")
		}
		
		private fun createType(type: Map<String, String>): Types? {
			try {
				if(type.containsKey("name") && type.containsKey("color")) {
					return Types(type["name"]!!, Color.valueOf(type["color"]!!))
				}
			} catch(e: Exception) {
				Warning("TODO", e)
			}
			return null
		}
		
		fun createTypes(data: List<Map<String, String>>) {
			data.forEach {
				val type = createType(it)
				type?.apply {
					types.add(this)
				}
			}
		}
	}
}
