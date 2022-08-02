package logic

import java.io.File
import java.io.FileReader

// TODO rework (this is a class instantiated once)

/**
 * Language
 *
 * @constructor Create empty Language
 * @property language
 */
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
	 * reads JSON from file and stores different Strings in translations Map
	 */
	init {
		val file = File({}::class.java.classLoader.getResource("lang/$language.json")!!.toURI())
		translations = (gson.fromJson<Map<String, Map<String, String>>>(
			getJsonReader(FileReader(file)),
			Map::class.java
		)).mapKeys { TranslationTypes.valueOf(it.key) }
	}


	/**
	 * Get translation
	 *
	 * @param tr
	 * @param type
	 * @return
	 */
	fun getTranslation(tr: String, type: TranslationTypes): String {
		@Suppress("SwallowedException")
		return try {
			translations[type]!![tr.lowercase()]!!
		} catch(e: NullPointerException) {
			val caller = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
				.walk { it.toList() }[2] // go back to getLangString and then calling methods
			log(
				"translation for |${
					tr.trim().lowercase()
				}| with type $type was not found (lang=$language) in (${caller.fileName}:${caller.lineNumber})",
				LogType.WARNING
			)
			tr // return requested string to translate
		}
	}

	/**
	 * Info
	 *
	 * @return
	 */
	fun info(): String = "Language: $language loaded ${translations.values.sumOf { it.size }} Translations"

	/**
	 * Available languages
	 *
	 * @constructor Create empty Available languages
	 */
	@Suppress("Unused")
	enum class AvailableLanguages {
		/**
		 * En
		 *
		 * @constructor Create empty En
		 */
		EN,

		/**
		 * De
		 *
		 * @constructor Create empty De
		 */
		DE,

		/**
		 * Fr
		 *
		 * @constructor Create empty Fr
		 */
		FR,
	}

	/**
	 * Translation types
	 *
	 * @constructor Create empty Translation types
	 */
	enum class TranslationTypes {
		/**
		 * Global
		 *
		 * @constructor Create empty Global
		 */
		Global,

		/**
		 * Menubar
		 *
		 * @constructor Create empty Menubar
		 */
		Menubar,

		/**
		 * Note
		 *
		 * @constructor Create empty Note
		 */
		Note,

		/**
		 * Overview
		 *
		 * @constructor Create empty Overview
		 */
		Overview,

		/**
		 * Reminder
		 *
		 * @constructor Create empty Reminder
		 */
		Reminder,

		/**
		 * Week
		 *
		 * @constructor Create empty Week
		 */
		Week,

		/**
		 * Appointment popup
		 *
		 * @constructor Create empty Appointment popup
		 */
		AppointmentPopup,

		/**
		 * Reminder popup
		 *
		 * @constructor Create empty Reminder popup
		 */
		ReminderPopup,
	}
}

/**
 * Translate
 *
 * @param type
 * @param args
 */
fun String.translate(type: Language.TranslationTypes, vararg args: Any?) =
	language.getTranslation(this, type).format(*args)
