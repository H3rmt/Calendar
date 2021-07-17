import frame.frameInit
import log.initConstants
import log.initLogger
import log.log

fun main(args: Array<String>) {
	log("starting Calendar: ${args.toSet()}")
	initConstants()
	initLogger()
	frameInit()
}