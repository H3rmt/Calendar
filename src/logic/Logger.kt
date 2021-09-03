package logic

import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.logging.ConsoleHandler
import java.util.logging.FileHandler
import java.util.logging.Formatter
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger
import java.util.stream.Stream



var logger: Logger? = null

lateinit var consoleHandler: ConsoleHandler
lateinit var fileHandler: FileHandler

fun updateLogger() {
	fileHandler.level = if(getConfig(Configs.Debug)) Level.ALL else Level.CONFIG
	consoleHandler.formatter = SimpleFormatter(getConfig(Configs.Logformat))
	fileHandler.formatter = consoleHandler.formatter
	if(!getConfig<Boolean>(Configs.Printlogs)) {
		logger?.removeHandler(consoleHandler)
	}
}

fun initLogger() {
	logger = Logger.getLogger("")
	logger?.apply {
		for(handler in handlers) {
			removeHandler(handler)  // remove predefined ConsoleHandler
		}
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
	logger?.apply {
		val callerlist: List<StackWalker.StackFrame> =
			StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(Stream<StackWalker.StackFrame>::toList)
		val caller = callerlist.filter { it.declaringClass.simpleName != "LoggerKt" }[0]
		var callerstr = caller.declaringClass.simpleName.ifBlank { caller.declaringClass.name.replaceBefore('.', "").substring(1) }
		callerstr += "." + caller.methodName + "(" + caller.fileName + ":" + caller.lineNumber + ")"
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
