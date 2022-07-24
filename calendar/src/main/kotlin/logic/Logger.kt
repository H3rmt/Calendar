package logic

import DEV
import logic.LogType.ERROR
import logic.LogType.IMPORTANT
import logic.LogType.LOW
import logic.LogType.NORMAL
import logic.LogType.WARNING
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.logging.ConsoleHandler
import java.util.logging.FileHandler
import java.util.logging.Formatter
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger


var logger: Logger = Logger.getLogger("")

lateinit var consoleHandler: ConsoleHandler
lateinit var fileHandler: FileHandler


/**
 * Initialises Logger with `default` data
 * - removes default Handlers
 * - adds default console and file Handler with hardcoded format
 *
 * is called before [configs] can be loaded with [initConfigs]
 *
 * @see logger
 * @see Files.logfile
 */
fun initLogger() {
	logger.apply {
		handlers.forEach { removeHandler(it) }

		level = if(DEV) Level.CONFIG else Level.INFO

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
 * Updates logger with values loaded from [configs] using formatting rules
 * [Configs.LogFormat] [Configs.DebugLogFormat]
 *
 * Removes file and console Handlers if specified in [Configs.PrintLogs]
 * and [Configs.StoreLogs] (unless [DEV] is enabled)
 *
 * @see logger
 */
fun updateLogger() {
	consoleHandler.formatter = SimpleFormatter(if(DEV) getConfig(Configs.DebugLogFormat) else getConfig(Configs.LogFormat))
	fileHandler.formatter = consoleHandler.formatter

	if(!getConfig<Boolean>(Configs.PrintLogs) && !DEV) {
		logger.removeHandler(consoleHandler)
	}

	if(getConfig(Configs.StoreLogs) || DEV) {
		logger.addHandler(fileHandler)
	}
}


/**
 * Log a message with specified LogType ([LogType.NORMAL] if not specified)
 *
 * [LogType]s are translated like:
 * - LogType.LOW -> Level.CONFIG
 * - LogType.NORMAL -> Level.INFO
 * - LogType.IMPORTANT -> Level.IMPORTANT
 * - LogType.WARNING -> Level.WARNING
 * - LogType.ERROR -> Level.SEVERE
 *
 * generates functionNames and fileNumbers for each log and passes it into
 * logger
 *
 * @param message
 * @param type
 */
fun log(message: Any?, type: LogType = NORMAL) {
	logger.apply {
		val callerList = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk { it.toList() }
		val caller = callerList.filter { it.declaringClass.simpleName != "LoggerKt" }[0]
		val callerStr = "(" + caller.fileName + ":" + caller.lineNumber + ")" + caller.methodName
		val mess = message.toString()
		val messstrip = message.toString().replace("\n", "\\n")

		when(type) {
			LOW -> log(Log(Level.CONFIG, messstrip, callerStr))
			NORMAL -> log(Log(Level.INFO, messstrip, callerStr))
			IMPORTANT -> log(Log(Important(), messstrip, callerStr))
			WARNING -> log(Log(Level.WARNING, messstrip, callerStr))
			ERROR -> log(Log(Level.SEVERE, mess, callerStr))
		}
	}
}

/**
 * custom Log to be passed into standard log method to override
 * getSourceClassName to use custom callString
 *
 * @param level loglevel, passed into LogRecord
 * @param msg message to be logged
 * @param caller caller (consisting of
 *     ({fileName}:{lineNumber}){methodName} )
 */
class Log(level: Level, msg: String, private val caller: String): LogRecord(level, msg) {
	override fun getSourceClassName(): String = caller
}


/**
 * Type of log to be logged
 *
 * types in descending order:
 * - [LOW] (lowest value)
 * - [NORMAL]
 * - [IMPORTANT]
 * - [WARNING]
 * - [ERROR] (highest value)
 */
enum class LogType {
	/**
	 * Messages only used for debugging (observable Lists, etc)
	 *
	 * = [Level.CONFIG] (700)
	 */
	LOW,

	/**
	 * Messages to provide basic Information about which state programm is
	 * currently executing, used for debugging purposes (opened Windows,
	 * popups, etc)
	 *
	 * = [Level.INFO] (800)
	 */
	NORMAL,

	/**
	 * Messages to provide important Information about which state programm
	 * is currently executing, or important modifications to Data (loading of
	 * configs, creation/deletion of Appointments/Notes)
	 *
	 * = [Important] (850)
	 */
	IMPORTANT,

	/**
	 * Messages providing information about problems, which don't cause the
	 * program to crash, but cause some parts to not function correctly
	 * (unknown Config Key, Image missing)
	 *
	 * = [Level.WARNING] (900)
	 */
	WARNING,

	/**
	 * Messages providing information about a serious Error, causing the
	 * Program to crash or exit (invalid Config File, missing Config value)
	 *
	 * = [Level.SEVERE] (1000)
	 */
	ERROR,
}

/**
 * Custom Level between [Level.INFO] and [Level.WARNING], to act as a more
 * relevant info
 *
 * @see LogType.IMPORTANT
 * @see Level
 */
class Important: Level("IMPORTANT", 850)

/**
 * Simple formatter used for extended formatting of LogRecords
 *
 * @param format format to format provided [LogRecord]s
 */
class SimpleFormatter(private val format: String): Formatter() {
	override fun format(record: LogRecord): String {
		val zdt = ZonedDateTime.ofInstant(record.instant, ZoneId.systemDefault())
		val source: String = record.sourceClassName ?: ""
		val message = formatMessage(record)
		return String.format(format, zdt, source, record.level.name, message)
	}
}
