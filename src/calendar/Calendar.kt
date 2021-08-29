package calendar

import javafx.beans.property.*
import javafx.collections.*
import logic.getJson
import logic.getJsonReader
import logic.getLangString
import logic.initCofigs
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.StringReader
import java.nio.charset.Charset
import java.security.SecureRandom
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.IsoFields
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import kotlin.random.Random



val now: ZonedDateTime = ZonedDateTime.now(ZoneId.systemDefault())

// -1 because clone and setMonth(true) is used for init
// and clone now
var calendardisplay: ZonedDateTime = now.minusMonths(1)

val currentmonth: ObservableList<Week> = FXCollections.observableArrayList()
val currentmonthName: SimpleStringProperty = SimpleStringProperty()

// https://www.baeldung.com/java-aes-encryption-decryption
fun generateKey(): SecretKey {
	val keyGenerator = KeyGenerator.getInstance("AES")
	keyGenerator.init(256)
	return keyGenerator.generateKey()
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


fun main() {
	initCofigs()
	setMonth(true)
	
	val save = getJson().toJson(currentmonth)
	println(save)
	
	val key: SecretKey = generateKey()
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
			
			if(Random.nextBoolean()) {
				for(num in 0..Random.nextInt(0, 10)) {
					days[days.size - 1].appointments.add(Appointment("Löng Text", Types.values().random()))
				}
			}
			
		} while(time.dayOfWeek.value != 1)
		
		val weekOfYear: Int = time.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
		
		val week = Week(time.minusDays(7), days[0], days[1], days[2], days[3], days[4], days[5], days[6], weekOfYear)
		weeks.add(week)
		//return weeks
	} while(time.month == monthtime.month && time.dayOfMonth > 1)
	
	return weeks
}
