package logic

import com.google.gson.stream.JsonReader
import java.io.File
import java.io.FileReader

class Language(private val language: Availablelanguages) {

	/**
	 * linking a String to a translated String
	 */
	var translations: Map<String, String>

	/**
	 * creates json file if it didn't exist
	 *
	 * reads JSON from file and stores different Strings
	 * in translations Map
	 *
	 * @see translations
	 */
	init {
		val file = File(getlanguagefile())
		if(! file.exists()) {
			file.createNewFile()
			val default = "{\n\n}"
			file.writeText(default)
		}
		val alltranslations: Map<String, Map<String, String>> = getJson().fromJson(JsonReader(FileReader(getlanguagefile())), Map::class.java)
		translations = alltranslations[language.toString()] ?: mapOf()
	}

	/**
	 * finds the corresponding translated String to a
	 * String
	 *
	 * @param translation String to translate
	 *
	 * @return translated String
	 *
	 * @see translations
	 */
	fun get(translation: String): String {
		try {
			translations[translation.trim().lowercase()] !!.let {
				return it
			}
		} catch (e: NullPointerException) {
			log("${translation.trim().lowercase()} was not found (lang=$language)", LogType.WARNING)
			return translation
		}
	}

	/**
	 * all different types of available Languages
	 *
	 * for a log.getLanguage to get loaded from JSON
	 * it must be specified here
	 */
	@Suppress("Unused")
	enum class Availablelanguages {
		en,
		de,
		fr,
	}

}