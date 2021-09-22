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
import kotlin.reflect.KVisibility
import kotlin.reflect.full.memberProperties


interface Celldisplay {
	//@Expose
	val notes: MutableList<Note>
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
	
	val time: ZonedDateTime = _time
	
	@Expose
	override val notes: MutableList<Note> = mutableListOf()
	
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
	
	fun getallappointments(): List<Appointment> {
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
	
	fun toDate(): String = "${time.dayOfMonth} - ${time.plusDays(6).dayOfMonth} / ${getLangString(time.month.name)}"
	
	fun addAppointments(appointmentslist: Map<DayOfWeek, List<Appointment>>) {
		for((key, value) in appointmentslist) {
			alldays[key]?.appointments?.addAll(value)
		}
	}
	
	override fun toString(): String {
		var s = "$notes | ${this::class.simpleName}{"
		this::class.memberProperties.filter { it.visibility != KVisibility.PRIVATE }
			.forEach { s += it.name + ":" + it.getter.call(this) + " " }
		return "$s}"
	}
}


class Day(_time: ZonedDateTime, _partofmonth: Boolean): Celldisplay {
	
	val time: ZonedDateTime = _time
	
	@Expose
	val partofmonth: Boolean = _partofmonth
	
	@Expose
	val appointments: MutableList<Appointment> = mutableListOf()
	
	@Expose
	override val notes: MutableList<Note> = mutableListOf()
	
	fun getappointmentslimit(): List<Appointment> = appointments.subList(0, minOf(appointments.size, getConfig<Double>(Configs.MaxDayAppointments).toInt()))
	
	override fun toString(): String {
		var s = "${this::class.simpleName}{"
		this::class.memberProperties.forEach { s += it.name + ":" + it.getter.call(this) + " " }
		return "$s}"
	}
}


class Appointment(_day: DayOfWeek, _start: Long, _duration: Long, _title: String, _description: String, _type: Types) {
	
	@Expose
	var day = _day
	
	@Expose
	var start = _start
	// stored in minutes instead of milliseconds (60 to 1)
	
	@Expose
	val duration = _duration
	// stored in minutes instead of seconds (60 to 1)
	
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


class Note(_time: Long, _text: String, _type: Types, _files: List<File>) {
	
	@Expose
	var time = _time
	// stored in minutes instead of milliseconds (60 to 1)
	
	@Expose
	val text = _text
	
	@Expose
	val type = _type
	
	@Expose
	val files = _files
	
	override fun toString(): String = "$time -text- $type  | ${files.toSet()}"
}

class File(_data: ByteArray, _name: String, _origin: String) {
	@Expose
	val data = _data
	
	@Expose
	val name = _name
	
	@Expose
	val origin = _origin
	
	constructor(file: java.io.File): this(file.inputStream().readAllBytes(), file.name, file.absolutePath)
	
	override fun toString(): String = "file: $name -> ${String(data)}"
}


class Types(_name: String, _color: Color) {
	
	val name: String = _name
	val color: Color = _color
	
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
			data.forEach {
				val type = createType(it)
				type?.apply {
					types.add(this)
					log("added type $type", LogType.LOW)
				}
			}
		}
	}
}