package logic

import DEV
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.logging.ConsoleHandler
import java.util.logging.FileHandler
import java.util.logging.Formatter
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger


/**
 * this is necessary to turn of printing of webkit performance by
 * com.sun.webkit.perf.PerfLogger as this can't be turned off, the default
 * logLevel is overridden to Off so the PerfLogger created will be disabled
 * and doesn't print performance because the PlatformLogger used for its
 * creation has a higher level than fine because they use the global
 * logging level as their logLevel on creation.
 */
//var logger: Logger = Logger.getGlobal()

var logger: Logger = Logger.getLogger("")

lateinit var consoleHandler: ConsoleHandler
lateinit var fileHandler: FileHandler


/** Update logger */
fun updateLogger() {
	logger.level = if(DEV) Level.CONFIG else Level.INFO

	consoleHandler.formatter = SimpleFormatter(if(DEV) getConfig(Configs.DebugLogFormat) else getConfig(Configs.LogFormat))
	fileHandler.formatter = consoleHandler.formatter

	if(!getConfig<Boolean>(Configs.PrintLogs) && !DEV) {
		logger.removeHandler(consoleHandler)
	}

	if(getConfig(Configs.StoreLogs) || DEV) {
		logger.addHandler(fileHandler)
	}
}

/** Init logger */
fun initLogger() {
	logger.apply {
		handlers.forEach { removeHandler(it) }

		level = Level.ALL

		consoleHandler = ConsoleHandler()
		consoleHandler.formatter = SimpleFormatter("[%1\$tT] |%3\$-10s %4\$s %n")
		addHandler(consoleHandler)
		log("added console Handler")

		fileHandler = FileHandler(Files.logfile)
		fileHandler.formatter = SimpleFormatter("[%1\$tT] |%3\$-10s %4\$s %n")
		fileHandler.level = Level.ALL
		log("added file Handler")
	}
}

/**
 * Log
 *
 * @param message
 * @param type
 */
fun log(message: Any?, type: LogType = LogType.NORMAL) {
	logger.apply {
		val callerList = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk { it.toList() }
		val caller = callerList.filter { it.declaringClass.simpleName != "LoggerKt" }[0]
		var callerStr = "(" + caller.fileName + ":" + caller.lineNumber + ")"
		callerStr += " " + caller.methodName

		val mess = message.toString()
		val messstrip = message.toString().replace("\n", "\\n")

		when(type) {
			LogType.LOW -> log(Log(Level.CONFIG, messstrip, callerStr))
			LogType.NORMAL -> log(Log(Level.INFO, messstrip, callerStr))
			LogType.IMPORTANT -> log(Log(Important(), messstrip, callerStr))
			LogType.WARNING -> log(Log(Level.WARNING, messstrip, callerStr))
			LogType.ERROR -> log(Log(Level.SEVERE, mess, callerStr))
		}
	}
}

/**
 * Log
 *
 * @param level
 * @param msg
 * @constructor
 * @property caller
 */
class Log(level: Level, msg: String, private val caller: String): LogRecord(level, msg) {
	override fun getSourceClassName(): String = caller
}


/**
 * Log type
 *
 * @constructor Create empty Log type
 */
enum class LogType {
	/**
	 * Low
	 *
	 * @constructor Create empty Low
	 */
	LOW,

	/**
	 * Normal
	 *
	 * @constructor Create empty Normal
	 */
	NORMAL,

	/**
	 * Important
	 *
	 * @constructor Create empty Important
	 */
	IMPORTANT,

	/**
	 * Warning
	 *
	 * @constructor Create empty Warning
	 */
	WARNING,

	/**
	 * Error
	 *
	 * @constructor Create empty Error
	 */
	ERROR,
}

/**
 * Important
 *
 * @constructor Create empty Important
 */
class Important: Level("IMPORTANT", 850)

/**
 * Simple formatter
 *
 * @constructor Create empty Simple formatter
 * @property format
 */
class SimpleFormatter(private val format: String): Formatter() {
	override fun format(record: LogRecord): String {
		val zdt = ZonedDateTime.ofInstant(record.instant, ZoneId.systemDefault())
		val source: String = record.sourceClassName ?: ""
		val message = formatMessage(record)
		return String.format(format, zdt, source, record.level.name, message)
	}
}
