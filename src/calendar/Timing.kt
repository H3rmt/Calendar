package calendar

import java.time.LocalDateTime
import java.time.Month
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime



object Timing {
	
	fun getNow(): ZonedDateTime {
		return ZonedDateTime.now(ZoneId.systemDefault())
	}
	
	fun getNowLocal(): LocalDateTime {
		return LocalDateTime.now(ZoneId.systemDefault())
	}
	
	fun LocalDateTime.toUTCEpochSecond(): Long {
		return toEpochSecond(ZoneOffset.UTC)
	}
	
	fun getNowUTC(year: Int, month: Month, dayOfMonth: Int, hour: Int): LocalDateTime {
		return LocalDateTime.of(year, month, dayOfMonth, hour, 0)
	}
}