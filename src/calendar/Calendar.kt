package calendar

import logic.getJson as Json
import javafx.beans.property.*
import javafx.collections.*
import logic.ConfigFiles
import logic.LogType
import logic.Warning
import logic.getJsonReader
import logic.getLangString
import logic.initCofigs
import logic.log
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
import java.time.ZoneId.systemDefault
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.temporal.IsoFields
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec


val now: ZonedDateTime = ZonedDateTime.now(systemDefault())

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
	
	val save = Json().toJson(currentmonth)
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
	val res: Any = Json().fromJson(getJsonReader(StringReader(plainText)), Any::class.java)
}


/**
 * called by buttons in calendar tab
 */
fun changeMonth(right: Boolean) {
	calendardisplay = if(right) {
		calendardisplay.plusMonths(1)
	} else {
		calendardisplay.plusMonths(-1)
	}
	currentmonthName.set(getLangString(calendardisplay.month.name))
	if(calendardisplay.year != now.year)
		currentmonthName.value += "  " + calendardisplay.year
	log("changing Month to ${calendardisplay.month.name}")
	
	val data = getMonth(calendardisplay)
	currentmonth.clear()
	currentmonth.addAll(data)
}

// Year: { Month: { day of month: Appointments } }
val preparedsingleAppointments: MutableMap<Int, MutableMap<Month, MutableMap<Int, MutableList<Appointment>>>> = mutableMapOf()

// Day: { Appointments }
val preparedweeklyAppointments: MutableMap<DayOfWeek, MutableList<Appointment>> = mutableMapOf()

// Year: { Month: { day of month: Appointments } }
val prepareddayNotes: MutableMap<Int, MutableMap<Month, MutableMap<Int, MutableList<Note>>>> = mutableMapOf()

// Year: { Week of year: Appointments }
val preparedweekNotes: MutableMap<Int, MutableMap<Int, MutableList<Note>>> = mutableMapOf()


fun createAppointment(appointment: Map<String, Any>, day: Boolean): Appointment? {
	try {
		return if(day) {
			Appointment(
				DayOfWeek.valueOf((appointment["day"] as String).uppercase()), (appointment["start"] as Double).toLong(),
				(appointment["duration"] as Double).toLong(), appointment["type"] as String,
				appointment["title"] as String, Types.valueOf(appointment["type"] as String)
			)
		} else {
			Appointment(
				DayOfWeek.SATURDAY, (appointment["start"] as Double).toLong(),
				(appointment["duration"] as Double).toLong(), appointment["type"] as String,
				appointment["title"] as String, Types.valueOf(appointment["type"] as String)
			)
		}
	} catch(e: Exception) {
		Warning("an35f7", e, "Exception creating Appointment from map:$appointment")
	}
	return null
}


fun prepareAppointments() {
	val read: Map<String, ArrayList<Map<String, Any>>> =
		Json().fromJson(getJsonReader(FileReader(ConfigFiles.appointmentsfile)), Map::class.java)
	
	read.forEach { (name, list) ->
		when(name) {
			"single Appointments" -> {
				log("reading single Appointments: $list", LogType.LOW)
				list.forEach {
					val appointment = createAppointment(it, false)
					appointment?.apply {
						val time = LocalDate.ofInstant(Instant.ofEpochSecond(start * 60), systemDefault())
						
						val offset: ZoneOffset = systemDefault().rules.getOffset(Instant.ofEpochSecond(start * 60))
						start -= time.atStartOfDay().toLocalTime().toEpochSecond(time, offset) / 60
						day = time.dayOfWeek
						
						if(!preparedsingleAppointments.containsKey(time.year))
							preparedsingleAppointments[time.year] = mutableMapOf()
						if(!preparedsingleAppointments[time.year]!!.containsKey(time.month))
							preparedsingleAppointments[time.year]!![time.month] = mutableMapOf()
						if(!preparedsingleAppointments[time.year]!![time.month]!!.containsKey(time.dayOfMonth))
							preparedsingleAppointments[time.year]!![time.month]!![time.dayOfMonth] = mutableListOf()
						
						preparedsingleAppointments[time.year]!![time.month]!![time.dayOfMonth]!!.add(this)
						log("loaded single Appointment: $this", LogType.LOW)
					}
				}
			}
			"Week Appointments" -> {
				log("reading Week Appointments: $list", LogType.LOW)
				list.forEach {
					val appointment = createAppointment(it, true)
					appointment?.apply {
						if(!preparedweeklyAppointments.containsKey(day))
							preparedweeklyAppointments[day] = mutableListOf()
						
						preparedweeklyAppointments[day]!!.add(this)
						log("loaded single Appointment: $this", LogType.LOW)
					}
				}
			}
		}
	}
	log("prepared week Appointments $preparedweeklyAppointments", LogType.NORMAL)
	log("prepared single Appointments $preparedsingleAppointments", LogType.NORMAL)
}


fun createNote(note: Map<String, Any>): Note? {
	try {
		return Note(
			(note["start"] as Double).toLong(),
			note["text"] as String,
			Types.valueOf(note["type"] as String)
		)
	} catch(e: Exception) {
		Warning("an35f7", e, "Exception creating Note from map:$note")
	}
	return null
}


fun preapareNotes() {
	val read: Map<String, ArrayList<Map<String, Any>>> =
		Json().fromJson(getJsonReader(FileReader(ConfigFiles.notesfile)), Map::class.java)
	
	read.forEach { (name, list) ->
		when(name) {
			"Day Notes" -> {
				log("reading Day Notes", LogType.LOW)
				list.forEach {
					val note = createNote(it)
					note?.apply {
						val time = LocalDate.ofInstant(Instant.ofEpochSecond(start * 60), systemDefault())
						
						if(!prepareddayNotes.containsKey(time.year))
							prepareddayNotes[time.year] = mutableMapOf()
						if(!prepareddayNotes[time.year]!!.containsKey(time.month))
							prepareddayNotes[time.year]!![time.month] = mutableMapOf()
						if(!prepareddayNotes[time.year]!![time.month]!!.containsKey(time.dayOfMonth))
							prepareddayNotes[time.year]!![time.month]!![time.dayOfMonth] = mutableListOf()
						
						prepareddayNotes[time.year]!![time.month]!![time.dayOfMonth]!!.add(this)
						log("loaded Day Note: $this", LogType.LOW)
					}
				}
			}
			"Week Notes" -> {
				log("reading Week Notes", LogType.LOW)
				list.forEach {
					val note = createNote(it)
					note?.apply {
						val time = LocalDate.ofInstant(Instant.ofEpochSecond(start * 60), systemDefault())
						
						if(!preparedweekNotes.containsKey(time.year))
							preparedweekNotes[time.year] = mutableMapOf()
						if(!preparedweekNotes[time.year]!!.containsKey(time.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)))
							preparedweekNotes[time.year]!![time.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)] = mutableListOf()
						
						preparedweekNotes[time.year]!![time.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)]!!.add(this)
						log("loaded week Note: $this", LogType.LOW)
					}
				}
			}
		}
	}
	log("prepared week Notes $preparedweekNotes", LogType.NORMAL)
	log("prepared day Notes $prepareddayNotes", LogType.NORMAL)
}


fun getMonth(monthtime: ZonedDateTime): MutableList<Week> {
	log("generating Month", LogType.LOW)
	var time: ZonedDateTime = monthtime.withDayOfMonth(1)
	val month = time.month
	
	val dayoffset = time.dayOfWeek.value
	time = time.minusDays((dayoffset - 1).toLong())
	
	val weeks: MutableList<Week> = mutableListOf()
	
	do {
		val days: MutableList<Day> = mutableListOf()
		do {
			val day = Day(time, time.month == month)
			day.appointments.addAll(preparedsingleAppointments[time.year]?.get(time.month)?.get(time.dayOfMonth) ?: listOf())
			day.notes.addAll(prepareddayNotes[time.year]?.get(time.month)?.get(time.dayOfMonth) ?: listOf())
			
			days.add(day)
			time = time.plusDays(1)
		} while(time.dayOfWeek.value != 1)
		
		val week = Week(
			time.minusDays(7),
			days[0],
			days[1],
			days[2],
			days[3],
			days[4],
			days[5],
			days[6],
			time.minusDays(7).get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
		)
		week.addAppointments(preparedweeklyAppointments)
		week.notes.addAll(preparedweekNotes[time.year]?.get(week.time.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)) ?: listOf())
		
		log("added week: $week", LogType.LOW)
		weeks.add(week)
	} while(time.month == monthtime.month && time.dayOfMonth > 1)
	
	return weeks
}
