package frame

import javafx.collections.ObservableList
import tornadofx.asObservable
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime

val now: ZonedDateTime = ZonedDateTime.now(ZoneId.systemDefault())

val currentmonth: ObservableList<Week> = mutableListOf<Week>().asObservable()

fun main() {
	println("${now.year}|${now.month}|${now.dayOfWeek}|${now.dayOfMonth}")

	val tmp = GetMonth(now.month.value)
	currentmonth.clear()
	currentmonth.addAll(tmp)
}


fun GetMonth(month: Int): MutableList<Week> {
	var Time: ZonedDateTime = now.withMonth(month).withDayOfMonth(1)

	val dayoffset = Time.dayOfWeek.value
	Time = Time.minusDays((dayoffset-1).toLong())

	val weeks: MutableList<Week> = mutableListOf()

	do {
		val newWeek: Week = Week()

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
			println("day ${Time.dayOfMonth}: ${Time.dayOfWeek}")

			Time = Time.plusDays(1)
		} while(Time.dayOfWeek.value != 1)

		weeks.add(weeks.size, newWeek)
	} while(Time.month.value == month && Time.dayOfMonth > 1)

	return weeks
}