import com.sun.javafx.application.LauncherImpl
import javafx.stage.*
import tornadofx.*


fun main() {
	LauncherImpl.launchApplication(TestWindow::class.java, null, emptyArray())
}

class TestWindow: App(TestMainView::class) {
	override fun start(stage: Stage) {
		stage.height = 350.0
		stage.width = 300.0
		super.start(stage)
	}
}

class TestMainView: View("Test") {
	override val root = hbox { add(DateTimePicker()) }
}

