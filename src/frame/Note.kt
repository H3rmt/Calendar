package frame

import calendar.Celldisplay
import calendar.Day
import calendar.Week
import javafx.scene.control.*
import javafx.scene.image.*
import javafx.scene.paint.*
import logic.LogType
import logic.getLangString
import logic.log
import tornadofx.*
import java.awt.image.BufferedImage
import java.io.File
import java.time.temporal.ChronoField
import javax.imageio.ImageIO



fun createnotetab(pane: TabPane, cell: Celldisplay): Tab {
	log("creating week tab", LogType.IMPORTANT)
	return pane.tab("") {
		if(cell is Day)
			text = "Notes for ${cell.time.dayOfMonth}. ${getLangString(cell.time.month.name)}"
		else if(cell is Week)
			text = "Notes for ${cell.time.get(ChronoField.ALIGNED_WEEK_OF_MONTH)}. Week in ${getLangString(cell.time.month.name)}"
		
		isClosable = true
		stackpane {
			style(append = true) {
				//maxHeight = 500.px
				padding = box(6.px)
			}
			
			vbox {
				style {
					borderColor += box(Color.TRANSPARENT)
					borderWidth += box(5.px)
					borderRadius += box(10.px)
				}
				/*webview {
					engine.load(File("img/note.svg").toURI().toASCIIString())
				}*/
				imageview(FXImage(ImageIO.read(File("img/note.svg")))) { }
				for(note in cell.notes) {
					label("$note + Text: ${note.text}")
				}
			}
			
			// used to shadow the overflow from tab
			pane {
				isMouseTransparent = true
				style {
					borderColor += box(Color.BLACK)
					borderWidth += box(5.px)
					borderRadius += box(10.px)
				}
			}
		}
	}
}


fun FXImage(image: BufferedImage, width: Int = image.width, height: Int = image.height): Image {
	val wr = WritableImage(image.width, image.height)
	val pw = wr.pixelWriter
	for(x in 0 until image.width) {
		for(y in 0 until image.height) {
			pw.setArgb(x, y, image.getRGB(x, y))
		}
	}
	return wr
}