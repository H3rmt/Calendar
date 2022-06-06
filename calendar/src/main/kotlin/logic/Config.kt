package logic

import com.google.gson.*
import com.google.gson.stream.JsonReader
import logic.ConfigFiles.dataDirectory
import java.io.*
import kotlin.collections.set
import kotlin.reflect.typeOf


private val gson: Gson =
	GsonBuilder().setPrettyPrinting().setLenient().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
		.excludeFieldsWithoutExposeAnnotation()
		.setFieldNamingStrategy(FieldNamingStrategy { return@FieldNamingStrategy it.name }).create()

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
 * @see ConfigFiles.configFile
 * @see ConfigFiles.dataDirectory
 */
fun initConfigs() {
	val file = File(ConfigFiles.configFile)
	if(!file.exists()) {
		if(dataDirectory.isNotEmpty()) {
			val dir = File(dataDirectory)
			dir.mkdirs()
		}
		file.createNewFile()
		file.writeText(CONFIG_DEFAULT)
		log("created default config:${ConfigFiles.configFile}", LogType.WARNING)
	}

	try {
		val load: Map<String, Any> =
			getJson().fromJson(getJsonReader(FileReader(ConfigFiles.configFile)), Map::class.java)
		load.forEach {
			try {
				configs[getJson().fromJson(
					getJsonReader(StringReader(it.key.trim().replaceFirstChar { c -> c.titlecaseChar() })),
					Configs::class.java
				)] = it.value
			} catch(e: NullPointerException) {
				Warning("g294n3", e, "Unknown config key: ${it.key}")
			}
			log("loaded Config ${it.key}: ${it.value}", LogType.LOW)
		}
	} catch(e: NullPointerException) {
		log("Config File missing", LogType.ERROR)
		throw Exit("5e928h", e)
	} catch(e: JsonSyntaxException) {
		log("JSON invalid in ConfigFile", LogType.ERROR)
		throw Exit("iu2sj2", e)
	}

	language = Language(getConfig(Configs.Language))
	log("loaded language ${language.info()}", LogType.LOW)

	stacktrace = getConfig(Configs.PrintStacktrace)
	log("set stacktrace $stacktrace", LogType.LOW)
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
			try {
				return if(T::class.java.isEnum) {
					try {
						getJson().fromJson<T>(getJsonReader(StringReader(it as String)), T::class.java)
					} catch(e: NullPointerException) {
						log("Unable to cast $it into element of ${T::class}", LogType.WARNING)
						throw Exit("??????", e)
					}
				} else {
					it as T
				}
			} catch(e: ClassCastException) {
				Warning(
					"ik49dk",
					e,
					"Invalid Config value: $conf requested: ${T::class.simpleName}  value: ${it::class.simpleName}"
				)
				if(T::class.supertypes.contains(typeOf<Number>()) && it::class.supertypes.contains(typeOf<Number>())) {
					log("Trying to use Gson to cast to Type: ${T::class.simpleName}", LogType.LOW)
					return getJson().fromJson(getJsonReader(StringReader(it.toString())), T::class.java)
				} else {
					throw Exit("k23d1f", e)
				}
			} catch(e: ClassCastException) {
				log(
					"Gson could not cast value: ${it::class.simpleName} to requested: ${T::class.simpleName}",
					LogType.WARNING
				)
				throw Exit("k23d1f", e)
			}
		}
		log("Missing Config option: $conf", LogType.WARNING)
		throw Exit("j21ka1")
	} catch(e: Exception) {
		log("Error reading Config option: $conf as ${T::class}", LogType.WARNING)
		throw Exit("??????", e)
	}
}

/**
 * is set to true at beginning of programm to prevent
 * stackoverflow if error produced before loading configuration
 * checks for stacktrace
 */
var stacktrace = true

/**
 * Custom Exception with Custom error code
 *
 * all codes listed in doc/error
 *
 * StackTrace can be disabled in config
 *
 * create:
 * Exit("g21k3m");
 * Exit("g21k3m", e)
 *
 * @see Exception
 *
 * @throws Exception
 */
class Exit(private val code: String, private val exception: Exception? = null): Exception(code) {

	override fun fillInStackTrace(): Throwable {
		return if(stacktrace)
			super.fillInStackTrace()
		else
			this
	}

	override fun toString(): String = "Exit <ErrorCode: $code> ${exception?.let { return@let "-> $it" } ?: ""}"
}


/**
 * Custom Warning with Custom error code
 * (doesn't stop the code)
 *
 * all codes listed in doc/error
 *
 * StackTrace can be disabled in config
 *
 * create:
 * Warning("k23d1f");
 * Warning("k23d1f", e)
 *
 * @see Exception
 *
 */
@Suppress("FunctionNaming", "UnusedPrivateMember")
fun Warning(code: String, exception: Exception, log: Any) {
	log(log, LogType.WARNING)
	val writer = StringWriter()

	if(stacktrace)
		exception.printStackTrace(PrintWriter(writer))
	else
		writer.append(exception.toString())

	log(writer, LogType.ERROR)
}

/**
 * only Configs in this Config enum are loaded from config.json
 */
enum class Configs {
	Language, Debug, PrintLogs, LogFormat, DebugLogFormat, StoreLogs, PrintStacktrace,
	AnimationSpeed, AnimationDelay, MaxDayAppointments, ExpandNotesOnOpen, IgnoreCaseForSearch
}

object ConfigFiles {
	const val logfile = "Calendar.log"

	const val dataDirectory = "data"

	const val languageFiles = "lang"

	const val configFile = "$dataDirectory/config.json"
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
		  "\t// \"[%1\$tF %1\$tT] |%3\$-10s %2\$-40s > %4\$s %n\",\n" +
		  "\t\"AnimationSpeed\": 200,\n" +
		  "\t\"AnimationDelay\": 80,\n" +
		  "\t\"MaxDayAppointments\": 8,\n" +
		  "\t\"ExpandNotesOnOpen\": true,\n" +
		  "\t\"IgnoreCaseForSearch\": true\n" +
		  "}"
