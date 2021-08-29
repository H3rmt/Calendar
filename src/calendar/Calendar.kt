package calendar

import javafx.beans.property.*
import javafx.collections.*
import logic.getJson
import logic.getJsonReader
import logic.getLangString
import logic.initCofigs
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileReader
import java.io.StringReader
import java.security.SecureRandom
import java.security.spec.KeySpec
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.Month
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.temporal.IsoFields
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec



val now: ZonedDateTime = ZonedDateTime.now(ZoneId.systemDefault())

// -1 because clone and setMonth(true) is used for init
// and clone now
var calendardisplay: ZonedDateTime = now.minusMonths(1)

val currentmonth: ObservableList<Week> = FXCollections.observableArrayList()
val currentmonthName: SimpleStringProperty = SimpleStringProperty()

// https://www.baeldung.com/java-aes-encryption-decryption

fun getKeyFromPassword(password: String): SecretKey {
	val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
	val spec: KeySpec = PBEKeySpec(
		password.toCharArray(),
		byteArrayOf(2, -127, 65, -67, -17, 65, 67, 36, -17, 65, -67, -17, -65, -67, -17, -65, -67, 20, 19),
		65536, 256
	)
	return SecretKeySpec(factory.generateSecret(spec).encoded, "AES")
}

fun generateIv(): IvParameterSpec {
	val iv = ByteArray(16)
	SecureRandom().nextBytes(iv)
	return IvParameterSpec(iv)
}

fun decrypt(algorithm: String, cipherText: ByteArray, key: SecretKey, iv: IvParameterSpec): ByteArray {
	val cipher = Cipher.getInstance(algorithm)
	cipher.init(Cipher.DECRYPT_MODE, key, iv)
	return cipher.doFinal(cipherText)
}

fun encrypt(algorithm: String, input: ByteArray, key: SecretKey, iv: IvParameterSpec): ByteArray {
	val cipher = Cipher.getInstance(algorithm)
	cipher.init(Cipher.ENCRYPT_MODE, key, iv)
	return cipher.doFinal(input)
}


fun maino() {
	initCofigs()
	setMonth(true)
	
	val save = getJson().toJson(currentmonth)
	println(save)
	
	val key: SecretKey = getKeyFromPassword("lulpswrd")
	val ivParameterSpec: IvParameterSpec = generateIv()
	val algorithm = "AES/CBC/PKCS5Padding"
	val cipherText = encrypt(algorithm, save.toByteArray(), key, ivParameterSpec)
	
	val fout = FileOutputStream("outp")
	fout.write(cipherText)
	fout.close()
	
	val fin = FileInputStream("outp")
	val read = fin.readAllBytes()
	println(read.toString(Charsets.UTF_8))
	fin.close()
	
	val plainText = String(decrypt(algorithm, read, key, ivParameterSpec))
	
	println(plainText)
	val res: Any = getJson().fromJson(getJsonReader(StringReader(plainText)), Any::class.java)
}



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

fun main() {
	prepareAppointments()
	println(preparedsingleAppointments)
	println(preparedweeklyAppointments)
}

// Year: { Month: { WeekDay: Appointments } }
val preparedsingleAppointments: MutableMap<Int, MutableMap<Month, MutableMap<DayOfWeek, MutableList<Appointment>>>> = mutableMapOf()

// Day: { Appointments }
val preparedweeklyAppointments: MutableMap<DayOfWeek, MutableList<Appointment>> = mutableMapOf()

fun createAppointment(appointment: Map<String, Any>): Appointment? {
	try {
		if(appointment.containsKey("day") && appointment.containsKey("start") &&
			appointment.containsKey("duration") && appointment.containsKey("type") &&
			appointment.containsKey("title") && appointment.containsKey("description")
		) {
			return Appointment(
				appointment["day"] as String, (appointment["start"] as Double).toLong(),
				(appointment["duration"] as Double).toLong(), appointment["type"] as String,
				appointment["title"] as String, Types.valueOf(appointment["type"] as String)
			)
		}
	} catch(e: Exception) {
		println(e)
	}
	return null
}

fun prepareAppointments() {
	val read: Map<String, ArrayList<Map<String, Any>>> = getJson().fromJson(getJsonReader(FileReader("data/test.json")), Map::class.java)
	
	read.forEach { (t, u) ->
		when(t) {
			"single Appointments" -> {
				u.forEach {
					val appointment = createAppointment(it)
					appointment?.apply {
						val time = LocalDate.ofInstant(Instant.ofEpochSecond(start * 60), ZoneId.systemDefault())
						
						val offset: ZoneOffset = ZoneId.systemDefault().rules.getOffset(Instant.ofEpochSecond(start * 60))
						start -= time.atStartOfDay().toLocalTime().toEpochSecond(time, offset) / 60
						
						if(!preparedsingleAppointments.containsKey(time.year))
							preparedsingleAppointments[time.year] = mutableMapOf()
						if(!preparedsingleAppointments[time.year]!!.containsKey(time.month))
							preparedsingleAppointments[time.year]!![time.month] = mutableMapOf()
						if(!preparedsingleAppointments[time.year]!![time.month]!!.containsKey(time.dayOfWeek))
							preparedsingleAppointments[time.year]!![time.month]!![time.dayOfWeek] = mutableListOf()
						
						preparedsingleAppointments[time.year]!![time.month]!![time.dayOfWeek]!!.add(this)
					}
				}
			}
			"Week Appointments" -> {
				// preparedweeklyAppointments
				u.forEach {
					val appointment = createAppointment(it)
					appointment?.apply {
						if(!preparedweeklyAppointments.containsKey(DayOfWeek.valueOf(day)))
							preparedweeklyAppointments[DayOfWeek.valueOf(day)] = mutableListOf()
						
						preparedweeklyAppointments[DayOfWeek.valueOf(day)]!!.add(this)
					}
				}
			}
		}
	}
}


fun getMonth(monthtime: ZonedDateTime): MutableList<Week> {
	var time: ZonedDateTime = monthtime.withDayOfMonth(1)
	val month = time.month
	
	val dayoffset = time.dayOfWeek.value
	time = time.minusDays((dayoffset - 1).toLong())
	
	val weeks: MutableList<Week> = mutableListOf()
	
	do {
		val days: MutableList<Day> = mutableListOf()
		do {
			days.add(Day(time, time.month == month))
			time = time.plusDays(1)
		} while(time.dayOfWeek.value != 1)
		
		val weekOfYear: Int = time.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
		
		val week = Week(time.minusDays(7), days[0], days[1], days[2], days[3], days[4], days[5], days[6], weekOfYear)
		weeks.add(week)
		//return weeks
	} while(time.month == monthtime.month && time.dayOfMonth > 1)
	
	return weeks
}
