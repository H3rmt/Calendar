package logic

import java.io.File
import java.io.FileReader

class Language(private val language: AvailableLanguages) {
	
	/**
	 * linking a String to a translated String
	 */
	private var translations: Map<String, Map<String, String>>
	
	private var translation: Map<TranslationTypes, Map<String, String>>
	
	/**
	 * creates json file if it didn't exist
	 *
	 * reads JSON from file and stores different Strings
	 * in translations Map
	 */
	init {
		val file = File({}::class.java.classLoader.getResource("${ConfigFiles.languageFiles}/$language.json").toURI())
		if(!file.exists()) {
			file.createNewFile()
			file.writeText(EMPTY_DEFAULT)
		}
		val allTranslations: Map<String, Map<String, String>> = getJson().fromJson(getJsonReader(FileReader(file)), Map::class.java)
		translations = allTranslations
		translation = (getJson().fromJson<Map<String, Map<String, String>>>(getJsonReader(FileReader(file)), Map::class.java)).mapKeys { TranslationTypes.valueOf(it.key) }
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
		val caller = transformClassname(StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk { it.toList() }[2].className)
		translations[caller]!![translation.trim().lowercase()]!!
	} catch(e: NullPointerException) {
		val caller = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk { it.toList() }[2] // go back to getLangString and then calling methods
		log(
			"translation for |${
				translation.trim().lowercase()
			}| was not found (lang=$language) in (${caller.fileName}:${caller.lineNumber})  [caller:${
				transformClassname(caller.className)
			}]",
			LogType.WARNING
		)
		translation // return requested string to translate
	}
	
	fun getTranslation(tr: String, type: TranslationTypes): String {
		return try {
			translation[type]!![tr]!!
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
	
	private fun transformClassname(name: String) = name.removeRange(
		name.indexOf('$').let { return@let if(it == -1) 0 else it },
		name.length
	)
	
	fun info(): String = "Language: $language loaded ${translation.values.sumOf { it.size }} Translations"
	
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
		Tab,
	}
}

fun String.translate(type: Language.TranslationTypes, vararg args: Any?) = language.getTranslation(this, type).format(*args)