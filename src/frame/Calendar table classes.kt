package frame


class Week {
	val Monday: Day? = null
	val Tuesday: Day? = Day()
	val Wednesday: Day? = Day()
	val Thursday: Day? = Day()
	val Friday: Day? = Day()
	val Saturday: Day? = Day()
	val Sunday: Day? = Day()
}

class Day {

	override fun toString(): String {
		return "hilul"
	}
}