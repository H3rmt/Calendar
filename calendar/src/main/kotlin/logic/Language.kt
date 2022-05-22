package logic

import java.io.File
import java.io.FileReader

class Language(private val language: AvailableLanguages) {
	
	/**
	 * Map<Category, Map<string, translation>>
	 *
	 * linking a String to a translated String, split by Category
	 */
	private var translations: Map<TranslationTypes, Map<String, String>>
	
	/**
	 * creates json file if it didn't exist
	 *
	 * reads JSON from file and stores different Strings
	 * in translations Map
	 */
	init {
		val file = File({}::class.java.classLoader.getResource("lang/$language.json")?.toURI() ?: throw Exit("?????"))
		if(!file.exists()) {
			file.createNewFile()
			file.writeText(EMPTY_LANGUAGE)
		}
		translations = (getJson().fromJson<Map<String, Map<String, String>>>(getJsonReader(FileReader(file)), Map::class.java)).mapKeys { TranslationTypes.valueOf(it.key) }
	}
	
	
	/**
	 * finds the corresponding translated String to a
	 * String
	 *
	 * @param tr String to translate
	 *
	 * @return translated String
	 *
	 * @see translations
	 */
	fun getTranslation(tr: String, type: TranslationTypes): String {
		return try {
			translations[type]!![tr]!!
		} catch(e: NullPointerException) {
			val caller = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk { it.toList() }[2] // go back to getLangString and then calling methods
			log(
				"translation for |${
					tr.trim().lowercase()
				}| was not found (lang=$language) in (${caller.fileName}:${caller.lineNumber})",
				LogType.WARNING
			)
			tr // return requested string to translate
		}
	}
	
	fun info(): String = "Language: $language loaded ${translations.values.sumOf { it.size }} Translations"
	
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
	
	enum class TranslationTypes {
		Global,
		Menubar,
		Note,
		Overview,
		Reminder,
		Week,
		AppointmentPopup,
		ReminderPopup,
	}
}

fun String.translate(type: Language.TranslationTypes, vararg args: Any?) = language.getTranslation(this, type).format(*args)