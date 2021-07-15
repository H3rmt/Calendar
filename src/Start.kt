import frame.frameInit
import log.initConstants
import log.initLogger
import log.log
import log.setUIManager

fun main(args: Array<String>) {
	log("starting Calendar: ${args.toSet()}")
	initConstants()
	initLogger()
	setUIManager()
	frameInit()
}