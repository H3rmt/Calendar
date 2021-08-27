package calendar

import javafx.scene.paint.*
import logic.getLangString
import java.time.ZonedDateTime


interface Celldisplay

class Week(_time: ZonedDateTime,Monday: Day, Tuesday: Day, Wednesday: Day, Thursday: Day, Friday: Day, Saturday: Day, Sunday: Day,val WeekofYear: Int): Celldisplay {
	
	val time: ZonedDateTime = _time
	
	var general = this
	
	val alldays = arrayOf(Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday)
	
	override fun toString(): String {
		return "${time.dayOfMonth} - ${time.plusDays(6).dayOfMonth} / ${getLangString(time.month.name)}"
	}
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
	Work,
	School;
	
	fun getColor(): Paint {
		return when(this) {
			Work -> Color.GOLD
			School -> Color.GREEN
		}
	}
}