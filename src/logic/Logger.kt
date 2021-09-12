package logic

import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.logging.ConsoleHandler
import java.util.logging.FileHandler
import java.util.logging.Formatter
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger


lateinit var logger: Logger

lateinit var consoleHandler: ConsoleHandler
lateinit var fileHandler: FileHandler

fun updateLogger() {
	/** this is necessary to turn of printing
	 * of webkit performance by com.sun.webkit.perf.PerfLogger
	 * as this can't be turned off, the default loglevel
	 * is overridden to Off so the PerfLogger created
	 * will be disabled and don't print performance because the
	 * Platformlogger used for their creation has a higher level
	 * than fine because they somehow use the global logging level
	 * as their loglevel on creation.
	 */
	logger.level = if(getConfig(Configs.Debug)) Level.ALL else Level.CONFIG
	
	consoleHandler.formatter = SimpleFormatter(getConfig(Configs.Logformat))
	fileHandler.formatter = consoleHandler.formatter
	
	if(!getConfig<Boolean>(Configs.Printlogs)) {
		logger.removeHandler(consoleHandler)
	}
}

fun initLogger() {
	logger = Logger.getLogger("")
	logger.apply {
		handlers.forEach { removeHandler(it) }
		
		level = Level.ALL
		
		consoleHandler = ConsoleHandler()
		consoleHandler.formatter = SimpleFormatter("[%1\$tF %1\$tT] |%3\$-10s %4\$s %n")
		addHandler(consoleHandler)
		log("added console Handler")
		
		fileHandler = FileHandler(ConfigFiles.logfile)
		fileHandler.formatter = SimpleFormatter("[%1\$tF %1\$tT] |%3\$-10s %4\$s %n")
		fileHandler.level = Level.ALL
		addHandler(fileHandler)
		
		fileHandler.publish(LogRecord(Level.INFO, "Logging start"))
		log("added file Handler")
	}
}

/**
 * adds a log message with a Logtype to the java Logger
 *
 * @param message gets send to the logger; doesn't have to be a string
 * @param type gets translated to java loglevels
 *
 * @see LogType
 */
fun log(message: Any, type: LogType = LogType.NORMAL) {
	logger.apply {
		val callerlist: List<StackWalker.StackFrame> = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk { it.toList() }
		val caller = callerlist.filter { it.declaringClass.simpleName != "LoggerKt" }[0]
		var callerstr = "(" + caller.fileName + ":" + caller.lineNumber + ")"
		/*callerstr += caller.declaringClass.simpleName.ifBlank { // strange formatting because nested $1$3$2 classes because tornadoFX
			println(caller.declaringClass.name); caller.declaringClass.name.run { substring(0, indexOf('$')) }.replaceBefore('.', "").substring(1)
		}*/
		callerstr += " " + caller.methodName
		
		when(type) {
			LogType.LOW -> log(Log(Level.CONFIG, "$message\t\t\t\t\t\t", callerstr))
			LogType.NORMAL -> log(Log(Level.INFO, "$message\t\t\t\t\t\t", callerstr))
			LogType.IMPORTANT -> log(Log(Important(), "$message\t\t\t\t\t\t", callerstr))
			LogType.WARNING -> log(Log(Level.WARNING, "$message\t\t\t\t\t\t", callerstr))
			LogType.ERROR -> log(Log(Level.SEVERE, "$message\t\t\t\t\t\t", callerstr))
		}
	}
	return
}

class Log(level: Level, msg: String, private val caller: String): LogRecord(level, msg) {
	override fun getSourceClassName(): String = caller
}


/**
 * different types of logs
 *
 * LOW : Used for information that will only get used if superunknown errors show up;
 *
 * NORMAL : Normal logs 🤷‍;
 *
 * WARNING : warnings something went wrong, but didn't break the programm
 *
 * ERROR : errors 🤷;
 */
enum class LogType {
	LOW,
	NORMAL,
	IMPORTANT,
	WARNING,
	ERROR,
}

class Important: Level("IMPORTANT", 850)

/**
 * full copy of the log.SimpleFormatter
 *
 * format of original log.SimpleFormatter was final and read of System props
 *
 * @see Formatter
 */
class SimpleFormatter(private val format: String): Formatter() {
	override fun format(record: LogRecord): String {
		val zdt = ZonedDateTime.ofInstant(record.instant, ZoneId.systemDefault())
		val source: String = record.sourceClassName ?: ""
		val message = formatMessage(record)
		return String.format(format, zdt, source, record.level.name, message)
	}
}
