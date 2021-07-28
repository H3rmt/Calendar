package frame

import java.time.ZoneId
import java.time.ZonedDateTime

val now: ZonedDateTime = ZonedDateTime.now(ZoneId.systemDefault())

fun main() {
	println("${now.year}|${now.month}|${now.dayOfWeek}|${now.dayOfMonth}")

	GetMonth(now.month.value)
	GetMonth(now.month.value + 1)
}

fun GetMonth(month: Int) {
	val now: ZonedDateTime = now.withMonth(month)

	println(now.month)
	println(now.dayOfWeek)
}