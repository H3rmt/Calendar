package frame

import java.awt.*
import java.awt.event.WindowEvent
import java.awt.geom.Arc2D
import java.awt.geom.Ellipse2D
import javax.swing.JFrame

val loadingScreen = LoadingScreen()
val loadingsCircles = arrayOf(0.0, 90.0, 180.0, 270.0)
val innerLoadingsCircles = arrayOf(0.0, 120.0, 240.0)
val thread: Thread = Thread {
	while(true) {
		try {
			Thread.sleep(30)
		} catch(_: InterruptedException) {
			break
		}
		for(i in (loadingsCircles.indices)) {
			loadingsCircles[i]++
		}
		for(i in (innerLoadingsCircles.indices)) {
			innerLoadingsCircles[i]--
		}
		loadingScreen.repaint()
	}
}

fun createLoading() {
	loadingScreen.isVisible = true
	thread.start()
}

fun removeLoading() {
	loadingScreen.dispatchEvent(WindowEvent(loadingScreen, WindowEvent.WINDOW_CLOSING))
	thread.interrupt()
}

class LoadingScreen: JFrame() {

	init {
		isVisible = false
		title = "Loading"
		isUndecorated = true
		shape = Ellipse2D.Double(0.0, 0.0, 100.0, 100.0)
		background = Color(0, 0, 0, 0)
		size = Dimension(100, 100)
		setLocationRelativeTo(null)
	}

	override fun paint(g: Graphics) {
		val g2 = g as Graphics2D
		g2.color = Color(0, 0, 0, 15)
		g2.fillRect(0, 0, width, height)

		g2.color = Color(60, 210, 0, 100)
		g2.stroke = BasicStroke(15F)

		g2.draw(Arc2D.Double(0.0, 0.0, width.toDouble(), height.toDouble(), loadingsCircles[0], 40.0, Arc2D.OPEN))
		g2.draw(Arc2D.Double(0.0, 0.0, width.toDouble(), height.toDouble(), loadingsCircles[1], 40.0, Arc2D.OPEN))
		g2.draw(Arc2D.Double(0.0, 0.0, width.toDouble(), height.toDouble(), loadingsCircles[2], 40.0, Arc2D.OPEN))
		g2.draw(Arc2D.Double(0.0, 0.0, width.toDouble(), height.toDouble(), loadingsCircles[3], 40.0, Arc2D.OPEN))


		g2.color = Color(60, 210, 0, 160)
		g2.stroke = BasicStroke(5F)
		g2.draw(
			Arc2D.Double(
				20.0, 20.0, width.toDouble() - 40, height.toDouble() - 40, innerLoadingsCircles[0], 50.0, Arc2D.PIE
			)
		)
		g2.draw(
			Arc2D.Double(
				20.0, 20.0, width.toDouble() - 40, height.toDouble() - 40, innerLoadingsCircles[1], 50.0, Arc2D.PIE
			)
		)
		g2.draw(
			Arc2D.Double(
				20.0, 20.0, width.toDouble() - 40, height.toDouble() - 40, innerLoadingsCircles[2], 50.0, Arc2D.PIE
			)
		)

	}
}
