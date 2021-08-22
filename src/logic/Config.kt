package logic

import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import com.google.gson.stream.JsonReader
import java.io.File
import java.io.FileReader
import java.io.Reader
import java.io.StringReader


private val gson: Gson = GsonBuilder().setPrettyPrinting().setLenient().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create()

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
var configs: MutableMap<Configs, String> = mutableMapOf()

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
		val default = "{\n" +
				"  \"language\": \"en\",\n" +
				"  \"debug\": \"false\",\n" +
				"  \"printstacktrace\": \"true\",\n" +
				"  \"printlogs\": \"true\",\n" +
				"  \"logformat\": \"\\\"[%1\$tF %1\$tT] |%4\$-10s %5\$s %n\\\"\" \n" +
				"}"
		file.writeText(default)
		log("created default config:${getconfigfile()}", LogType.WARNING)
	}
	
	try {
		val load: Map<String, String> = getJson().fromJson(getJsonReader(FileReader(getconfigfile())), Map::class.java)
		load.forEach {
			try {
				configs[getJson().fromJson(getJsonReader(StringReader(it.key.trim())), Configs::class.java)] = it
					.value.trim()
			} catch(e: NullPointerException) {
				log("Unknown config key: $it Code: g294n3", LogType.WARNING)
			}
		}
	} catch(e: NullPointerException) {
		log("Config File missing $e Code: 5e928h", LogType.ERROR)
		throw Exit("5e928h")
	} catch(e: JsonSyntaxException) {
		log("JSON invalid ${e.message}  Code: iu2sj2", LogType.ERROR)
		throw Exit("iu2sj2")
	}
	
	language = Language(getConfig(Configs.language))
	
	stacktrace = getConfig(Configs.printstacktrace)
}

/**
 * returns a configuration in Config enum specified in config.json
 * cast to given type
 *
 * log.getConfig<ConfigType>(log.Configs.<config>)
 *
 * ConfigType = Int / String / Boolean / Enum element
 *
 * config = Enum Element
 *
 * @see Configs
 * @see configs
 */
inline fun <reified T: Any> getConfig(conf: Configs): T {
	if(configs[conf] != null)
		try {
			return getJson().fromJson(getJsonReader(StringReader(configs[conf] ?: "")), T::class.java)
		} catch(e: java.lang.NullPointerException) {
			log("Invalid Config value: $conf")
			throw Exit("k23d1f")
		} catch(e: JsonSyntaxException) {
			log("Invalid Json Syntax: $conf  | ${e.message} ")
			throw Exit("gf30ik")
		}
	else {
		log("Missing Config option: $conf")
		throw Exit("j21ka1")
	}
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
 * log.Exit("g21k3m")
 *
 * @see Exception
 */
class Exit(private val text: String): Exception(text) {
	
	override fun fillInStackTrace(): Throwable {
		return if(stacktrace)
			super.fillInStackTrace()
		else
			this
		
	}
	
	override fun toString(): String {
		return "Exception | ErrorCode: $text"
	}
}


/**
 * only Configs in this Config enum are loaded from config.json
 *
 * have to be all lowercase
 */
enum class Configs {
	language, debug, printlogs, logformat, printstacktrace,
}

fun getlogfile(): String = "Calendar.log"

fun getdatadirectory(): String = "data"

fun getlanguagefile(): String = getdatadirectory() + "/lang.json"

fun getconfigfile(): String = getdatadirectory() + "/config.json"

lateinit var language: Language

/**
 * returns a String that was translated into the general log.Language
 *
 * @see Language
 */
fun getLangString(str: String): String {
	return language.get(str)
}
