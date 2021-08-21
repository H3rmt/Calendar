package calendar

import java.time.ZonedDateTime


interface Celldisplay

class Week(Monday: Day, Tuesday: Day, Wednesday: Day, Thursday: Day, Friday: Day, Saturday: Day, Sunday: Day): Celldisplay {
	
	var general = this
	
	val alldays = arrayOf(Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday)
}

class Day(_time: ZonedDateTime): Celldisplay {
	
	val time: ZonedDateTime = _time
	
	val appointments: MutableList<Appointment> = mutableListOf()
	
	override fun toString(): String {
		return "${time.dayOfMonth}:${time.dayOfWeek}"
	}
}

class Appointment(val _description: String, val _type: Types) {

}

enum class Types {
	Work, School
}