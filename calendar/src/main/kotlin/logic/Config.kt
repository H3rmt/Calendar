package logic

import DEV
import com.google.gson.FieldNamingStrategy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import com.google.gson.stream.JsonReader
import java.io.File
import java.io.FileReader
import java.io.Reader
import java.io.StringReader
import kotlin.collections.set
import kotlin.reflect.typeOf


private val gson: Gson = GsonBuilder().setLenient().setFieldNamingStrategy(FieldNamingStrategy { return@FieldNamingStrategy it.name }).create()

/**
 * general JSON reader and writer
 */
fun getJson() = gson

/**
 * returns configured JSON reader
 * accepts " " in Strings
 */
fun getJsonReader(reader: Reader): JsonReader = JsonReader(reader).apply { isLenient = true }

/**
 * map that links A config with a JSON string
 * of (Int / String / Boolean / Enum element)
 *
 * @see Configs
 */
var configs: MutableMap<Configs, Any> = mutableMapOf()

/**
 * must be the first method called to read from data files
 * like fonts or language
 *
 * @see Files.configFile
 */
fun initConfigs() {
	val file = File(Files.configFile)
	if(!file.exists()) {
		if(!File(OSFolders.getConfigFolder()).exists()) {
			val dir = File(OSFolders.getConfigFolder())
			dir.mkdirs()
		}
		file.createNewFile()
		file.writeText(CONFIG_DEFAULT)
		log("created default config:${Files.configFile} in ${File(OSFolders.getConfigFolder())}", LogType.WARNING)
	}

	try {
		val load: Map<String, Any> = getJson().fromJson(getJsonReader(FileReader(Files.configFile)), Map::class.java)
		load.forEach {
			@Suppress("SwallowedException")
			try {
				configs[getJson().fromJson(
					getJsonReader(StringReader(it.key.trim().replaceFirstChar { c -> c.titlecaseChar() })),
					Configs::class.java
				)] = it.value
			} catch(e: NullPointerException) {
				log("Unknown config key: ${it.key}", LogType.WARNING)
			}
			log("loaded Config ${it.key}: ${it.value}", LogType.LOW)
		}
	} catch(e: JsonSyntaxException) {
		log("JSON invalid in ConfigFile", LogType.ERROR)
		throw e
	}

	language = Language(getConfig(Configs.Language))
	log("loaded language ${language.info()}")

	stacktrace = getConfig(Configs.PrintStacktrace)
	log("set stacktrace $stacktrace")
}

/**
 * returns a configuration in Config enum specified in config.json
 * cast to given type
 *
 * getConfig<ConfigType>(Configs.<config>)
 *
 * ConfigType = Int / String / Boolean / Enum element
 *
 * enums get cast automatically from String,
 * other types throw errors if type doesn't match
 *
 * config = Enum Element
 *
 * @see Configs
 * @see configs
 */
@Suppress("NestedBlockDepth", "ThrowsCount", "TooGenericExceptionCaught")
inline fun <reified T: Any> getConfig(conf: Configs): T {
	try {
		configs[conf]?.let {
			@Suppress("SwallowedException")
			try {
				return if(T::class.java.isEnum) {
					try {
						getJson().fromJson<T>(getJsonReader(StringReader(it as String)), T::class.java)
					} catch(e: NullPointerException) {
						log("Unable to cast $it into element of ${T::class}", LogType.WARNING)
						throw e
					}
				} else {
					it as T
				}
			} catch(e: ClassCastException) {
				log(
					"Invalid Config value: $conf requested: ${T::class.simpleName}  value: ${it::class.simpleName}",
					LogType.WARNING
				)
				if(T::class.supertypes.contains(typeOf<Number>()) && it::class.supertypes.contains(typeOf<Number>())) {
					return getJson().fromJson(getJsonReader(StringReader(it.toString())), T::class.java)
				} else {
					throw ClassCastException()
				}
			} catch(e: ClassCastException) {
				log(
					"Gson could not cast value: ${it::class.simpleName} to requested: ${T::class.simpleName}",
					LogType.WARNING
				)
				throw e
			}
		}
		log("Missing Config option: $conf", LogType.ERROR)
		throw IllegalArgumentException("Missing Config option")
	} catch(e: Exception) {
		log("Error reading Config option: $conf as ${T::class}", LogType.ERROR)
		throw e
	}
}

/**
 * is set to true at beginning of programm to prevent
 * stackoverflow if error produced before loading configuration
 * checks for stacktrace
 */
var stacktrace = true

/**
 * only Configs in this Config enum are loaded from config.json
 */
enum class Configs {
	Language, PrintLogs, LogFormat, DebugLogFormat, StoreLogs, PrintStacktrace,
	AnimationSpeed, AnimationDelay, MaxDayAppointments, ExpandNotesOnOpen, IgnoreCaseForSearch
}

object Files {
	val logfile = if(DEV) "./calendar.log" else OSFolders.getDataFolder() + "calendar.log"
	val DBfile = if(DEV) "./data.sqlite" else OSFolders.getDataFolder() + "data.sqlite"
	val configFile = if(DEV) "./config.json" else OSFolders.getConfigFolder() + "config.json"
}

lateinit var language: Language

// TODO update this before release
const val CONFIG_DEFAULT = "{\n" +
		"\t\"Language\": \"EN\",\n" +
		"\t\"Debug\": false,\n" +
		"\t\"PrintStacktrace\": true,\n" +
		"\t\"PrintLogs\": true,\n" +
		"\t\"StoreLogs\": true,\n" +
		"\t\"LogFormat\": \"[%1\$tF %1\$tT] |%3\$-10s %2\$-40s > %4\$s %n\",\n" +
		"\t\"DebugLogFormat\": \"[%1\$tF %1\$tT] |%3\$-10s %2\$-40s > %4\$s %n\",\n" +
		"\t\"AnimationSpeed\": 200,\n" +
		"\t\"AnimationDelay\": 80,\n" +
		"\t\"MaxDayAppointments\": 8,\n" +
		"\t\"ExpandNotesOnOpen\": true,\n" +
		"\t\"IgnoreCaseForSearch\": true\n" +
		"}"
