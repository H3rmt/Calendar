package logic

import java.io.PrintWriter
import java.io.StringWriter
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.logging.ConsoleHandler
import java.util.logging.FileHandler
import java.util.logging.Formatter
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger


var logger: Logger? = null

fun initLogger() {
	val formatter = SimpleFormatter()
	
	logger = Logger.getLogger("")
	
	logger?.let {
		it.removeHandler(it.handlers[0])  // remove ConsoleHandler
		it.level = Level.ALL
		
		if(getConfig(Configs.Printlogs)) {
			val consoleHandler = ConsoleHandler()
			consoleHandler.level = Level.INFO
			consoleHandler.formatter = formatter
			it.addHandler(consoleHandler)
		}
		
		val fileHandler = FileHandler(getlogfile())
		fileHandler.formatter = formatter
		if(getConfig(Configs.Debug))
			fileHandler.level = Level.ALL
		else
			fileHandler.level = Level.CONFIG
		it.addHandler(fileHandler)
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
	logger?.let {
		when(type) {
			LogType.LOW -> it.log(Level.CONFIG, message.toString())
			LogType.NORMAL -> it.log(Level.INFO, message.toString())
			LogType.IMPORTANT -> it.log(Important(), message.toString())
			LogType.WARNING -> it.log(Level.WARNING, message.toString())
			LogType.ERROR -> it.log(Level.SEVERE, message.toString())
		}
		//if(type != LogType.ERROR)
			return
	}
	println("${LocalDateTime.now()} | $type  $message")
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
class SimpleFormatter: Formatter() {
	private val format: String = getConfig(Configs.Logformat)
	
	override fun format(record: LogRecord): String {
		val zdt = ZonedDateTime.ofInstant(
			record.instant, ZoneId.systemDefault()
		)
		var source: String?
		if(record.sourceClassName != null) {
			source = record.sourceClassName
			if(record.sourceMethodName != null) {
				source += " " + record.sourceMethodName
			}
		} else {
			source = record.loggerName
		}
		val message = formatMessage(record)
		var throwable = ""
		if(record.thrown != null) {
			val sw = StringWriter()
			val pw = PrintWriter(sw)
			pw.println()
			record.thrown.printStackTrace(pw)
			pw.close()
			throwable = sw.toString()
		}
		return String.format(
			format,
			zdt,
			source,
			record.loggerName,
			record.level.localizedName,
			message,
			throwable
		)
	}
}
