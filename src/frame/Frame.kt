package frame


import log.getLangString
import java.awt.Dimension
import javax.swing.JFrame
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem
import javax.swing.WindowConstants

import tornadofx.*

val frame: JFrame = JFrame("Calendar")

val menubar: JMenuBar = JMenuBar()

val optionsmenu: JMenu = JMenu(getLangString("options"))
val optionsmenurefresh: JMenuItem = JMenuItem(getLangString("refresh"))

val viewmenu: JMenu = JMenu(getLangString("view"))
val viewmenushow: JMenu = JMenu(getLangString("show"))
val viewmenushowitems = listOf(JMenuItem(getLangString("show calendar")), JMenuItem(getLangString("show reminders")))

fun frameInit() {
	frame.setLocationRelativeTo(null)
	frame.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
	frame.isVisible = false
	frame.maximumSize = Dimension(500, 500)
	frame.minimumSize = Dimension(100, 100)

	optionsmenu.add(optionsmenurefresh)
	menubar.add(optionsmenu)

	for(item in viewmenushowitems)
		viewmenushow.add(item)
	viewmenu.add(viewmenushow)
	menubar.add(viewmenu)

	frame.jMenuBar = menubar

	//frame.isVisible = true
	frame.setSize(300, 200)

}
