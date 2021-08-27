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
import kotlin.collections.set
import kotlin.reflect.typeOf


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
		val default = "{\n" +
				"  \"language\": \"en\",\n" +
				"  \"debug\": false,\n" +
				"  \"printstacktrace\": true,\n" +
				"  \"printlogs\": true,\n" +
				"  \"logformat\": \"[%1\$tF %1\$tT] |%4\$-10s %5\$s %n\", \n" +
				"  \"Animationspeed\": 300,\n" +
				"  \"Animationdelay\": 80\n" +
				"}"
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
				//Warning("g294n3")
			}
		}
	} catch(e: NullPointerException) {
		log("Config File missing", LogType.ERROR)
		throw Exit("5e928h", e)
	} catch(e: JsonSyntaxException) {
		log("JSON invalid in Configfile", LogType.ERROR)
		throw Exit("iu2sj2", e)
	}
	
	language = Language(getConfig(Configs.Language))
	
	stacktrace = getConfig(Configs.Printstacktrace)
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
				//TODO Warning
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
		return "Exception <ErrorCode: $code> ${exception?.let { return@let "-> $it" } ?: ""}"
	}
}


/**
 * only Configs in this Config enum are loaded from config.json
 */
enum class Configs {
	Language, Debug, Printlogs, Logformat, Printstacktrace, Animationspeed, Animationdelay
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
	return language.get(str)
}
