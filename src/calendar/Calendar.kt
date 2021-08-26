package calendar

import javafx.beans.property.*
import javafx.collections.*
import logic.getLangString
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.IsoFields
import kotlin.random.Random



val now: ZonedDateTime = ZonedDateTime.now(ZoneId.systemDefault())

// -1 because clone and setMonth(true) is used for init
// and clone now
var calendardisplay: ZonedDateTime = now.minusMonths(1)

val currentmonth: ObservableList<Week> = FXCollections.observableArrayList()
val currentmonthName: SimpleStringProperty = SimpleStringProperty()


/**
 * called by buttons in calendar tab
 */
fun setMonth(right: Boolean) {
	calendardisplay = if(right) {
		calendardisplay.plusMonths(1)
	} else {
		calendardisplay.plusMonths(-1)
	}
	currentmonthName.set(getLangString(calendardisplay.month.name))
	if(calendardisplay.year != now.year)
		currentmonthName.value += "  " + calendardisplay.year
	
	val data = getMonth(calendardisplay)
	currentmonth.clear()
	currentmonth.addAll(data)
}


fun getMonth(monthtime: ZonedDateTime): MutableList<Week> {
	var time: ZonedDateTime = monthtime.withDayOfMonth(1)
	
	val dayoffset = time.dayOfWeek.value
	time = time.minusDays((dayoffset - 1).toLong())
	
	val weeks: MutableList<Week> = mutableListOf()
	
	do {
		val days: MutableList<Day> = mutableListOf()
		do {
			days.add(Day(time))
			time = time.plusDays(1)
			
			if(Random.nextBoolean()) {
				for(num in 0..Random.nextInt(0, 4)) {
					days[days.size - 1].appointments.add(Appointment("Arbeit Text ttttt", Types.School))
				}
			}
			
		} while(time.dayOfWeek.value != 1)
		
		val weekOfYear: Int = time.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
		
		val week = Week(days[0], days[1], days[2], days[3], days[4], days[5], days[6], weekOfYear)
		weeks.add(week)
	} while(time.month == monthtime.month && time.dayOfMonth > 1)
	
	return weeks
}
