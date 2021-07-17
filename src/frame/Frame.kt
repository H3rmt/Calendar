package frame


import frame.Styles
import javafx.geometry.Pos
import javafx.stage.Stage
import log.getLangString
import tornadofx.*
import java.awt.Dimension
import javax.swing.JFrame
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem
import javax.swing.WindowConstants

val frame: JFrame = JFrame("Calendar")

val menubar: JMenuBar = JMenuBar()

val optionsmenu: JMenu = JMenu(getLangString("options"))
val optionsmenurefresh: JMenuItem = JMenuItem(getLangString("refresh"))

val viewmenu: JMenu = JMenu(getLangString("view"))
val viewmenushow: JMenu = JMenu(getLangString("show"))
val viewmenushowitems = listOf(JMenuItem(getLangString("show calendar")), JMenuItem(getLangString("show reminders")))

fun frameInitold() {
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

class Application: App(MainView::class, Styles::class) {
	init {
		reloadStylesheetsOnFocus()
	}

	override fun start(stage: Stage) {
		stage.height = 300.0
		stage.width = 500.0
		super.start(stage)
	}
}

fun frameInit() {
	launch<Application>()
}

class MainView: View("Calendar") {
	override val root = hbox(spacing = 12, alignment = Pos.CENTER) {
		label(title) {
			addClass(Styles.header)
		}

		button {
			text = "hi"
			action {
				println("ff")
			}
		}
	}
}

