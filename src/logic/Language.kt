package logic

import com.google.gson.stream.JsonReader
import java.io.File
import java.io.FileReader

class Language(private val language: Availablelanguages) {
	
	/**
	 * linking a String to a translated String
	 */
	private var translations: Map<String, String>
	
	/**
	 * creates json file if it didn't exist
	 *
	 * reads JSON from file and stores different Strings
	 * in translations Map
	 *
	 * @see translations
	 */
	init {
		val file = File(ConfigFiles.languagefile)
		if(!file.exists()) {
			file.createNewFile()
			file.writeText(emptydefault)
		}
		val alltranslations: Map<String, Map<String, String>> =
			getJson().fromJson(JsonReader(FileReader(ConfigFiles.languagefile)), Map::class.java)
		translations = alltranslations["$language"] ?: mapOf()
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
	operator fun get(translation: String): String {
		try {
			translations[translation.trim().lowercase()]!!.let {
				return it
			}
		} catch(e: NullPointerException) {
			log("translation for ${translation.trim().lowercase()} was not found (lang=$language)", LogType.WARNING)
			return translation
		}
	}
	
	override fun toString(): String = "Language: $language loaded ${translations.size} Translations"
	
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