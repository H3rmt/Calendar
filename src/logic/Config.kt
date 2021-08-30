package logic

import calendar.Types
import com.google.gson.FieldNamingPolicy
import com.google.gson.FieldNamingStrategy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import com.google.gson.stream.JsonReader
import java.io.File
import java.io.FileReader
import java.io.PrintWriter
import java.io.Reader
import java.io.StringReader
import java.io.StringWriter
import kotlin.collections.set
import kotlin.reflect.typeOf


private val gson: Gson = GsonBuilder()/*.setPrettyPrinting()*/.setLenient().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
	.excludeFieldsWithoutExposeAnnotation().setFieldNamingStrategy(FieldNamingStrategy { return@FieldNamingStrategy it.name }).create()

/**
 * general JSON reader and writer
 */
fun getJson() = gson

/**
 * returns configured JSON reader
 * accepts " " in Strings
 */
fun getJsonReader(reader: Reader): JsonReader {
	val retreader = JsonReader(reader)
	retreader.isLenient = true
	return retreader
}

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
 * @see getconfigfile
 * @see getdatadirectory
 */
fun initCofigs() {
	val file = File(getconfigfile())
	if(!file.exists()) {
		if(getdatadirectory() != "") {
			val dir = File(getdatadirectory())
			dir.mkdirs()
		}
		file.createNewFile()
		file.writeText(default)
		log("created default config:${getconfigfile()}", LogType.WARNING)
	}
	
	try {
		val load: Map<String, Any> = getJson().fromJson(getJsonReader(FileReader(getconfigfile())), Map::class.java)
		load.forEach {
			try {
				configs[getJson().fromJson(getJsonReader(StringReader(it.key.trim().capitalize())), Configs::class.java)] = it.value
			} catch(e: NullPointerException) {
				log("Unknown config key: ${it.key}", LogType.WARNING)
				Warning("g294n3", e)
			}
			log("Loaded Config ${it.key}: ${it.value}", LogType.LOW)
		}
	} catch(e: NullPointerException) {
		log("Config File missing", LogType.ERROR)
		throw Exit("5e928h", e)
	} catch(e: JsonSyntaxException) {
		log("JSON invalid in Configfile", LogType.ERROR)
		throw Exit("iu2sj2", e)
	}
	
	language = Language(getConfig(Configs.Language))
	log("loaded language $language", LogType.LOW)
	
	stacktrace = getConfig(Configs.Printstacktrace)
	log("set stacktrace $stacktrace", LogType.LOW)
	
	Types.createTypes(getConfig(Configs.Appointmenttypes))
	log("loaded Types ${Types.getTypes()}", LogType.LOW)
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
@OptIn(ExperimentalStdlibApi::class)
inline fun <reified T: Any> getConfig(conf: Configs): T {
	configs[conf]?.let {
		try {
			try {
				return if(T::class.java.isEnum) {
					getJson().fromJson(getJsonReader(StringReader(it as String)), T::class.java)
				} else {
					it as T
				}
			} catch(e: ClassCastException) {
				log("Invalid Config value: $conf requested: ${T::class.simpleName}  value: ${it::class.simpleName}", LogType.WARNING)
				Warning("TODO()", e)
				if(T::class.supertypes.contains(typeOf<Number>()) && it::class.supertypes.contains(typeOf<Number>())) {
					log("Trying to use Gson to cast to Type: ${T::class.simpleName}", LogType.WARNING)
					return getJson().fromJson(getJsonReader(StringReader(it.toString())), T::class.java)
				} else
					throw e
			}
		} catch(e1: ClassCastException) {
			log("Gson could not cast value: ${it::class.simpleName} to requested: ${T::class.simpleName}", LogType.WARNING)
			throw Exit("k23d1f", e1)
		}
	}
	log("Missing Config option: $conf", LogType.WARNING)
	throw Exit("j21ka1")
}

/**
 * is set to true at beginning of programm to prevent
 * stackoverflow if error produced bevore loading configuration
 * checks for stacktrace
 */
var stacktrace = true

/**
 * Custom Exception with Custom error code
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
	
	override fun toString(): String {
		return "Exit <ErrorCode: $code> ${exception?.let { return@let "-> $it" } ?: ""}"
	}
}

fun Warning(code: String, exception: Exception) {
	try {
		throw Exit(code, exception)
	} catch(e: Exit) {
		val writer = StringWriter()
		
		if(getConfig(Configs.Printstacktrace))
			e.printStackTrace(PrintWriter(writer))
		else
			writer.append(e.toString())
		
		log(writer, LogType.ERROR)
	}
}

/**
 * only Configs in this Config enum are loaded from config.json
 */
enum class Configs {
	Language, Debug, Printlogs, Logformat, Printstacktrace,
	Animationspeed, Animationdelay, MaxDayAppointments, Appointmenttypes
}

fun getlogfile(): String = "Calendar.log"

fun getdatadirectory(): String = "data"

fun getlanguagefile(): String = getdatadirectory() + "/lang.json"

fun getconfigfile(): String = getdatadirectory() + "/config.json"

lateinit var language: Language

/**
 * returns a String that was translated into the general Language
 *
 * @see Language
 */
fun getLangString(str: String): String {
	return language[str]
}

const val default = "{\n" +
		"\t\"Language\": \"en\",\n" +
		"\t\"debug\": false,\n" +
		"\t\"printstacktrace\": true,\n" +
		"\t\"printlogs\": true,\n" +
		"\t\"logformat\": \"[%1\$tF %1\$tT] |%3\$-10s %4\$s %n\",\n" +
		"\t\"Animationspeed\": 300,\n" +
		"\t\"Animationdelay\": 120,\n" +
		"\t\"MaxDayAppointments\": 8,\n" +
		"\t\"Appointmenttypes\": [\n" +
		"\t\t{\n" +
		"\t\t\t\"name\": \"Work\",\n" +
		"\t\t\t\"color\": \"BLUE\"\n" +
		"\t\t},\n" +
		"\t\t{\n" +
		"\t\t\t\"name\": \"Sport\",\n" +
		"\t\t\t\"color\": \"BLACK\"\n" +
		"\t\t},\n" +
		"\t\t{\n" +
		"\t\t\t\"name\": \"School\",\n" +
		"\t\t\t\"color\": \"RED\"\n" +
		"\t\t}\n" +
		"\t]\n" +
		"}"
