import frame.frameInit
import logic.LogType
import logic.configs
import logic.initCofigs
import logic.initLogger
import logic.log
import kotlin.system.exitProcess

fun main(args: Array<String>) {
	log("starting Calendar: ${args.toSet()}", LogType.IMPORTANT)
	initCofigs()
	log("read Configs:$configs", LogType.IMPORTANT)
	initLogger()
	log("starting Frame", LogType.IMPORTANT)
	frameInit()
	exitProcess(1)
}