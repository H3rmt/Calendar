package frame

import java.awt.Dimension
import javax.swing.JFrame
import javax.swing.WindowConstants

val frame : JFrame = JFrame("Calendar")

fun frameInit() {
	frame.setLocationRelativeTo(null)
	frame.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
	frame.isVisible = false
	frame.maximumSize = Dimension(500, 500)
}