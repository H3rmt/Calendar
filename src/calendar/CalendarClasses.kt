package calendar

import com.google.gson.annotations.Expose
import javafx.scene.paint.*
import logic.Configs
import logic.LogType
import logic.Warning
import logic.getConfig
import logic.getLangString
import logic.log
import java.time.DayOfWeek
import java.time.DayOfWeek.FRIDAY
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.SATURDAY
import java.time.DayOfWeek.SUNDAY
import java.time.DayOfWeek.THURSDAY
import java.time.DayOfWeek.TUESDAY
import java.time.DayOfWeek.WEDNESDAY
import java.time.ZonedDateTime


interface Celldisplay {
	//@Expose
	val notes: MutableList<Note>
	
	val time: ZonedDateTime
}

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
	
	override val time: ZonedDateTime = _time
	
	@Expose
	override val notes: MutableList<Note> = mutableListOf()
	
	@Expose
	val allDays: Map<DayOfWeek, Day> = mapOf(
		MONDAY to Monday,
		TUESDAY to Tuesday,
		WEDNESDAY to Wednesday,
		THURSDAY to Thursday,
		FRIDAY to Friday,
		SATURDAY to Saturday,
		SUNDAY to Sunday
	)
	
	fun getallAppointments(): List<Appointment> {
		val list = mutableListOf<Appointment>()
		for(day in allDays.values) {
			list.addAll(day.appointments)
		}
		return list
	}
	
	fun getallAppointmentsSorted(): Map<Types, List<Appointment>> {
		val list = mutableMapOf<Types, MutableList<Appointment>>()
		for(appointment in getallAppointments()) {
			if(list[appointment.type] == null)
				list[appointment.type] = mutableListOf()
			list[appointment.type]!!.add(appointment)
		}
		return list
	}
	
	fun toDate(): String = "${time.dayOfMonth} - ${time.plusDays(6).dayOfMonth} / ${getLangString(time.month.name)}"
	
	fun addAppointments(appointmentslist: Map<DayOfWeek, List<Appointment>>) {
		for((key, value) in appointmentslist) {
			allDays[key]?.appointments?.addAll(value)
		}
	}
	
	override fun toString(): String = "$time $notes $allDays"
}


data class Day(override val time: ZonedDateTime, val partofmonth: Boolean): Celldisplay {
	
	@Expose
	val appointments: MutableList<Appointment> = mutableListOf()
	
	@Expose
	override val notes: MutableList<Note> = mutableListOf()
	
	fun getAppointmentsLimited(): List<Appointment> = appointments.subList(0, minOf(appointments.size, getConfig<Double>(Configs.MaxDayAppointments).toInt()))
	
	override fun toString(): String = "$time $notes $appointments"
}


data class Appointment(
	@Expose var day: DayOfWeek,
	@Expose var start: Long,
	@Expose val duration: Long,
	@Expose val title: String,
	@Expose val description: String,
	@Expose val type: Types
) {
	
	override fun toString(): String = "$day: $start - $duration  $type | $title: $description"
}


data class Note(
	@Expose var time: Long,
	@Expose var text: String,
	@Expose val type: Types,
	@Expose val files: List<File>
) {
	
	override fun toString(): String = "$time $type $files"
}

data class File(
	@Expose val data: ByteArray,
	@Expose val name: String,
	@Expose val origin: String
) {
	
	@Suppress("unused")
	constructor(file: java.io.File): this(file.inputStream().readAllBytes(), file.name, file.absolutePath)
	
	override fun toString(): String = "$name ${data.size} $origin"
}


data class Types(
	@Expose val name: String,
	@Expose val color: Color
) {
	
	override fun toString(): String = name
	
	companion object {
		
		private var types: MutableList<Types> = mutableListOf()
		
		fun getTypes(): List<Types> = types.toCollection(mutableListOf())
		
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
				Warning("o2wi35", e, "Exception creating Type from map:$type")
			}
			return null
		}
		
		fun createTypes(data: List<Map<String, String>>) {
			types.clear()
			data.forEach {
				val type = createType(it)
				type?.apply {
					types.add(this)
					log("added type $type", LogType.LOW)
				}
			}
			log("created types $types", LogType.NORMAL)
		}
	}
}