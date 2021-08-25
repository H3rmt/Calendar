import frame.frameInit
import logic.LogType
import logic.configs
import logic.initCofigs
import logic.initLogger
import logic.log

fun main(args: Array<String>) {
	println("\n\tStarting Calendar... \n")
	log("starting Calendar: ${args.toSet()}", LogType.IMPORTANT)
	initCofigs()
	log("read Configs:$configs", LogType.IMPORTANT)
	initLogger()
	log("starting Frame", LogType.IMPORTANT)
	frameInit()
}