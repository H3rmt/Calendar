package calendar

import javafx.scene.paint.*
import logic.getLangString
import java.time.ZonedDateTime


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
			if(list[appointment._type] == null)
				list[appointment._type] = mutableListOf()
			list[appointment._type]!!.add(appointment)
		}
		return list
	}
	
	override fun toString(): String {
		return "${time.dayOfMonth} - ${time.plusDays(6).dayOfMonth} / ${getLangString(time.month.name)}"
	}
}

class Day(_time: ZonedDateTime, val partofmonth: Boolean): Celldisplay {
	
	val time: ZonedDateTime = _time
	
	val appointments: MutableList<Appointment> = mutableListOf()
	
	override fun toString(): String {
		return "${time.dayOfMonth}:${time.dayOfWeek}"
	}
}

class Appointment(val _description: String, val _type: Types) {

}

enum class Types {
	Work,
	Private,
	School;
	
	fun getColor(): Paint {
		return when(this) {
			Work -> Color.BLUE
			Private -> Color.BLACK
			School -> Color.RED
		}
	}
}