package frame

import java.time.ZonedDateTime


class Week {
	lateinit var Monday: Day
	lateinit var  Tuesday: Day
	lateinit var  Wednesday: Day
	lateinit var  Thursday: Day
	lateinit var  Friday: Day
	lateinit var  Saturday: Day
	lateinit var  Sunday: Day

	var self: Week = this
}

class Day(_time: ZonedDateTime) {

	val time: ZonedDateTime = _time

	override fun toString(): String {
		return "${time.dayOfMonth}"//:${time.dayOfWeek}"
	}
}