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
	 * Get translation or return non-translated string
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
	 * returns info about loaded language
	 *
	 * @return
	 */
	fun info(): String = "Language: $language loaded ${translations.values.sumOf { it.size }} Translations"

	/** Available languages with translations */
	@Suppress("Unused")
	enum class AvailableLanguages {
		/** English language */
		EN,

		/** German language */
		DE,

		/** French language */
		FR,
	}

	/**
	 * Translations are divided into categories to better distinguish between
	 * different places where translations are used
	 */
	enum class TranslationTypes {
		/** Translation used everywhere (Month or Day translations) */
		Global,

		/** Translations used in the Menubar */
		Menubar,

		/** Translations used in the Note Tab */
		Note,

		/** Translations used in the Overview Tab */
		Overview,

		/** Translations used in the Reminder Tab */
		Reminder,

		/** Translations used in the Week Tab */
		Week,

		/** Translations used in the AppointmentPopup */
		AppointmentPopup,

		/** Translations used in the ReminderPopup */
		ReminderPopup,
	}
}

/**
 * Extension function to translate a string into the global language with
 * some vararg which are formatted into the string (%s, %t)
 *
 * @param type [Language.TranslationTypes] type where translation is used
 * @param args
 *
 * @see String.format
 */
fun String.translate(type: Language.TranslationTypes, vararg args: Any?) =
	language.getTranslation(this, type).format(*args)
