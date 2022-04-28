package calendar

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import java.time.ZoneId
import java.time.ZoneOffset


object Timing {
	fun getNowLocal(): LocalDateTime = LocalDateTime.now(ZoneId.systemDefault())
	
	fun LocalDate.toUTCEpochMinute(): Long = toEpochSecond(LocalTime.MIDNIGHT, ZoneOffset.UTC) / 60
	
	fun LocalDateTime.toUTCEpochMinute(): Long = toEpochSecond(ZoneOffset.UTC) / 60
	
	/**
	 * get LocalDateTime of year month and day of month and hour
	 *
	 */
	fun getNowUTC(year: Int, month: Month, dayOfMonth: Int, hour: Int): LocalDateTime = LocalDateTime.of(year, month, dayOfMonth, hour, 0)
	
	fun fromUTCEpochMinuteToLocalDateTime(start: Long): LocalDateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(start * 60), ZoneOffset.UTC)
}
