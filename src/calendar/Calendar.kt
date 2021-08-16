package calendar

import frame.currentmonth
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime


val now: ZonedDateTime = ZonedDateTime.now(ZoneId.systemDefault())

fun main() {
	val data = GetMonth(now.month.value)
	currentmonth.clear()
	currentmonth.addAll(data)

//	val fout = FileOutputStream("")
//	val dout = DataOutputStream(fout)

//	fout.close()
//	dout.close()

}


fun GetMonth(month: Int): MutableList<Week> {
	var Time: ZonedDateTime = now.withMonth(month).withDayOfMonth(1)
	
	val dayoffset = Time.dayOfWeek.value
	Time = Time.minusDays((dayoffset - 1).toLong())
	
	val weeks: MutableList<Week> = mutableListOf()
	
	do {
		val newWeek = Week()
		
		do {
			when(Time.dayOfWeek) {
				DayOfWeek.MONDAY -> newWeek.Monday = Day(Time.plusHours(0))
				DayOfWeek.TUESDAY -> newWeek.Tuesday = Day(Time.plusHours(0))
				DayOfWeek.WEDNESDAY -> newWeek.Wednesday = Day(Time.plusHours(0))
				DayOfWeek.THURSDAY -> newWeek.Thursday = Day(Time.plusHours(0))
				DayOfWeek.FRIDAY -> newWeek.Friday = Day(Time.plusHours(0))
				DayOfWeek.SATURDAY -> newWeek.Saturday = Day(Time.plusHours(0))
				DayOfWeek.SUNDAY -> newWeek.Sunday = Day(Time.plusHours(0))
				else -> continue
			}
			Time = Time.plusDays(1)
		} while(Time.dayOfWeek.value != 1)
		
		newWeek.Friday.appointments = listOf(
			Appointment("Arb", Types.Work),
			Appointment("School", Types.School),
			Appointment("School", Types.School),
			Appointment("Arb 3", Types.Work)
		)
		newWeek.Saturday.appointments = listOf(Appointment("Arb 2", Types.Work), Appointment("Arb 3", Types.Work))
		newWeek.Thursday.appointments = listOf(Appointment("Mathe", Types.School))
		
		weeks.add(weeks.size, newWeek)
	} while(Time.month.value == month && Time.dayOfMonth > 1)
	
	return weeks
}