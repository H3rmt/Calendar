import frame.frameInit
import logic.initConstants
import logic.initLogger
import logic.log

fun main(args: Array<String>) {
	log("starting Calendar: ${args.toSet()}")
	initConstants()
	initLogger()
	frameInit()
}