package logic

import java.io.File
import java.io.FileReader

class Language(private val language: AvailableLanguages) {
	
	/**
	 * linking a String to a translated String
	 */
	private var translations: Map<String, String>
	
	/**
	 * creates json file if it didn't exist
	 *
	 * reads JSON from file and stores different Strings
	 * in translations Map
	 */
	init {
		val file = File(ConfigFiles.languageFile)
		if(!file.exists()) {
			file.createNewFile()
			file.writeText(emptyDefault)
		}
		val allTranslations: Map<String, Map<String, String>> =
			getJson().fromJson(getJsonReader(FileReader(ConfigFiles.languageFile)), Map::class.java)
		translations = allTranslations["$language"] ?: mapOf()
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
	operator fun get(translation: String): String = try {
		translations[translation.trim().lowercase()]!!
	} catch(e: NullPointerException) {
		log("translation for |${translation.trim().lowercase()}| was not found (lang=$language)", LogType.WARNING)
		translation // return requested string to translate
	}
	
	
	override fun toString(): String = "Language: $language loaded ${translations.size} Translations"
	
	/**
	 * all different types of available Languages
	 *
	 * for a log.getLanguage to get loaded from JSON
	 * it must be specified here
	 */
	@Suppress("Unused")
	enum class AvailableLanguages {
		EN,
		DE,
		FR,
	}
	
}