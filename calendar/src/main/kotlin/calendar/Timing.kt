package calendar

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset


object Timing {
	/** Current LocalDateTime */
	fun getNow(): LocalDateTime = LocalDateTime.now(ZoneId.systemDefault())

	/** converts a LocalDate to a Long for storage in DB */
	fun LocalDate.toUTCEpochMinute(): Long = toEpochSecond(LocalTime.MIDNIGHT, ZoneOffset.UTC) / 60

	/** converts a LocalDateTime to a Long for storage in DB */
	fun LocalDateTime.toUTCEpochMinute(): Long = toEpochSecond(ZoneOffset.UTC) / 60

	/** converts a Long to a LocalDateTime from storage in DB */
	fun fromUTCEpochMinuteToLocalDateTime(start: Long): LocalDateTime =
		LocalDateTime.ofInstant(Instant.ofEpochSecond(start * 60), ZoneOffset.UTC)

	/** converts a Long to a LocalDate from storage in DB */
	fun fromUTCEpochMinuteToLocalDate(start: Long): LocalDate =
		LocalDate.ofInstant(Instant.ofEpochSecond(start * 60), ZoneOffset.UTC)
}
