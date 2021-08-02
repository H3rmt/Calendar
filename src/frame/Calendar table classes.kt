package frame

import java.time.ZonedDateTime


class Week {
	var Monday: Day? = null
	var Tuesday: Day? = null
	var Wednesday: Day? = null
	var Thursday: Day? = null
	var Friday: Day? = null
	var Saturday: Day? = null
	var Sunday: Day? = null
}

class Day(_time: ZonedDateTime) {

	val time: ZonedDateTime = _time

	override fun toString(): String {
		return "${time.dayOfMonth}"//:${time.dayOfWeek}"
	}
}