package frame

import calendar.Celldisplay
import calendar.Day
import calendar.Week
import javafx.scene.control.*
import javafx.scene.image.*
import javafx.scene.paint.*
import logic.LogType
import logic.Warning
import logic.getLangString
import logic.log
import tornadofx.*
import java.awt.image.BufferedImage
import java.io.File
import java.time.temporal.ChronoField
import javax.imageio.IIOException
import javax.imageio.ImageIO
import kotlin.random.Random



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

/**
 * <path,image>
 */
val cache = mutableMapOf<String, Image>()

fun FXImage(path: String): Image {
	cache[path]?.let { return it }
	val image = try {
		ImageIO.read(File(path).takeIf { it.exists() })
	} catch(e: IllegalArgumentException) {
		Warning("imageerror", e, "file not found:$path")
		imagemissing()
	} catch(e: IIOException) {
		Warning("imageerror", e, "can't read file:$path")
		imagemissing()
	}
	val wr = WritableImage(image.width, image.height)
	val pw = wr.pixelWriter
	for(x in 0 until image.width) {
		for(y in 0 until image.height) {
			pw.setArgb(x, y, image.getRGB(x, y))
		}
	}
	cache.putIfAbsent(path, wr)
	return wr
}

fun imagemissing(): BufferedImage {
	val im = BufferedImage(10, 30, BufferedImage.TYPE_3BYTE_BGR)
	val g2 = im.graphics
	for(i in 0 until 10)
		for(j in 0 until 30) {
			g2.color = java.awt.Color(Random.nextInt(255), Random.nextInt(255), Random.nextInt(255))
			g2.drawRect(i, j, 1, 1)
		}
	g2.dispose()
	return im
}