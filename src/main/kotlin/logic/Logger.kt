package logic

import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.logging.ConsoleHandler
import java.util.logging.FileHandler
import java.util.logging.Formatter
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger


/** this is necessary to turn of printing
 * of webkit performance by com.sun.webkit.perf.PerfLogger
 * as this can't be turned off, the default logLevel
 * is overridden to Off so the PerfLogger created
 * will be disabled and doesn't print performance because the
 * PlatformLogger used for its creation has a higher level
 * than fine because they use the global logging level
 * as their logLevel on creation.
 */
//var logger: Logger = Logger.getGlobal()

var logger: Logger = Logger.getLogger("")

lateinit var consoleHandler: ConsoleHandler
lateinit var fileHandler: FileHandler

fun updateLogger() {
	logger.level = if(getConfig(Configs.Debug)) Level.ALL else Level.CONFIG
	
	consoleHandler.formatter = SimpleFormatter(if(getConfig(Configs.Debug)) getConfig(Configs.DebugLogFormat) else getConfig(Configs.LogFormat))
	fileHandler.formatter = consoleHandler.formatter
	
	if(!getConfig<Boolean>(Configs.PrintLogs)) {
		logger.removeHandler(consoleHandler)
	}
	
	if(getConfig(Configs.StoreLogs)) {
		logger.addHandler(fileHandler)
	}
}

fun initLogger() {
	logger.apply {
		handlers.forEach { removeHandler(it) }
		
		level = Level.ALL
		
		consoleHandler = ConsoleHandler()
		consoleHandler.formatter = SimpleFormatter("[%1\$tT] |%3\$-10s %4\$s %n")
		addHandler(consoleHandler)
		log("added console Handler")
		
		fileHandler = FileHandler(ConfigFiles.logfile)
		fileHandler.formatter = SimpleFormatter("[%1\$tT] |%3\$-10s %4\$s %n")
		fileHandler.level = Level.ALL
		
		log("added file Handler")
	}
}

/**
 * adds a log message with a LogType to the java Logger
 *
 * @param message gets send to the logger; doesn't have to be a string
 * @param type gets translated to java logLevels
 *
 * @see LogType
 */
fun log(message: Any?, type: LogType = LogType.NORMAL) {
	logger.apply {
		val callerList = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk { it.toList() }
		val caller = callerList.filter { it.declaringClass.simpleName != "LoggerKt" }[0]
		var callerStr = "(" + caller.fileName + ":" + caller.lineNumber + ")"
		/*callerStr += caller.declaringClass.simpleName.ifBlank { // strange formatting because nested $1$3$2 classes because tornadoFX
			println(caller.declaringClass.name); caller.declaringClass.name.run { substring(0, indexOf('$')) }.replaceBefore('.', "").substring(1)
		}*/
		callerStr += " " + caller.methodName
		
		
		val mess = message.toString().replace("\n", "\\n ")
		
		when(type) {
			LogType.LOW -> log(Log(Level.CONFIG, mess, callerStr))
			LogType.NORMAL -> log(Log(Level.INFO, mess, callerStr))
			LogType.IMPORTANT -> log(Log(Important(), mess, callerStr))
			LogType.WARNING -> log(Log(Level.WARNING, mess, callerStr))
			LogType.ERROR -> log(Log(Level.SEVERE, mess, callerStr))
		}
	}
}

class Log(level: Level, msg: String, private val caller: String): LogRecord(level, msg) {
	override fun getSourceClassName(): String = caller
}


/**
 * different types of logs
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
