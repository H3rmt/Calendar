package logic

import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.logging.ConsoleHandler
import java.util.logging.FileHandler
import java.util.logging.Formatter
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger



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
		
		fileHandler = FileHandler(getlogfile())
		fileHandler.formatter = SimpleFormatter("[%1\$tF %1\$tT] |%3\$-10s %4\$s %n")
		fileHandler.level = Level.ALL
		addHandler(fileHandler)
	}
}

/**
 * adds a log.log to the logList, so it gets logged later on
 *
 * @param message gets written in the log.log; doesn't necessarily have to be a string
 * @param type if this is higher or equal to the current loglevel than this log.log will be accepted
 *
 * @see LogType
 */
fun log(message: Any, type: LogType = LogType.NORMAL) {
	logger?.apply {
		val caller: StackWalker.StackFrame =
			StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk { s -> s.skip(1).findFirst() }.get()
		var callerstr = caller.declaringClass.simpleName.ifBlank { caller.declaringClass.name.replaceBefore('.', "").substring(1) }
		callerstr += "." + caller.methodName + "(" + caller.fileName + ":" + caller.lineNumber + ")"
		when(type) {
			LogType.LOW -> log(Log(Level.CONFIG, message.toString(), callerstr))
			LogType.NORMAL -> log(Log(Level.INFO, message.toString(), callerstr))
			LogType.IMPORTANT -> log(Log(Important(), message.toString(), callerstr))
			LogType.WARNING -> log(Log(Level.WARNING, message.toString(), callerstr))
			LogType.ERROR -> log(Log(Level.SEVERE, message.toString(), callerstr))
		}
	}
	return
}

class Log(level: Level, msg: String, private val caller: String): LogRecord(level, msg) {
	override fun getSourceClassName(): String {
		return caller
	}
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
		val source: String = record.sourceClassName
		val message = formatMessage(record)
		return String.format(format, zdt, source, record.level.name, message)
	}
}
