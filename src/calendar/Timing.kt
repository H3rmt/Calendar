package calendar

import java.time.LocalDateTime
import java.time.Month
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime



object Timing {
	
	fun getNow(): ZonedDateTime = ZonedDateTime.now(ZoneId.systemDefault())
	
	fun getNowLocal(): LocalDateTime = LocalDateTime.now(ZoneId.systemDefault())
	
	fun LocalDateTime.toUTCEpochMinute(): Long = toEpochSecond(ZoneOffset.UTC) / 60
	
	fun getNowUTC(year: Int, month: Month, dayOfMonth: Int, hour: Int): LocalDateTime = LocalDateTime.of(year, month, dayOfMonth, hour, 0)
}