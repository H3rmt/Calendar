package calendar

import javafx.animation.*
import java.time.ZonedDateTime


class Week(Monday: Day, Tuesday: Day, Wednesday: Day, Thursday: Day, Friday: Day, Saturday: Day, Sunday: Day): Celldisplay {
	var Monday: Day = Monday
	var Tuesday: Day = Tuesday
	var Wednesday: Day = Wednesday
	var Thursday: Day = Thursday
	var Friday: Day = Friday
	var Saturday: Day = Saturday
	var Sunday: Day = Sunday
	
	var general = this
	
	val alldays = arrayOf(Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday)
}

interface Celldisplay {
}

class Day(_time: ZonedDateTime): Celldisplay {
	
	val time: ZonedDateTime = _time
	
	val appointments: MutableList<Appointment> = mutableListOf()
	
	val appointmentopenanimations: MutableList<PathTransition> = mutableListOf()
	val appointmentcloseanimations: MutableList<PathTransition> = mutableListOf()
	
	override fun toString(): String {
		return "${time.dayOfMonth}:${time.dayOfWeek}"
	}
}

class Appointment(val _description: String, val _type: Types) {

}

enum class Types {
	Work, School
}