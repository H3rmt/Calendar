package frame.picker.dateTimePicker

import frame.createFXImage
import javafx.beans.property.*
import javafx.event.*
import javafx.scene.control.*
import javafx.scene.paint.*
import logic.ObservableValueListeners.listen
import tornadofx.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * Extension function for creating an [DateTimePicker] with observable list
 * of appointments to let the user choose from
 *
 * @param dateTime property to contain currently selected dateTime
 * @param formatter formatter to format text to the textfield
 * @see DateTimePicker
 * @see LocalDateTime
 */
fun EventTarget.dateTimePicker(
	dateTime: Property<LocalDateTime> = LocalDateTime.now().toProperty(),
	formatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT),
	op: DateTimePicker.() -> Unit = {}
): DateTimePicker {
	val picker = DateTimePicker(dateTime, formatter)
	return opcr(this, picker, op)
}

/**
 * class representing the DateTime picker deriving from the Control Node
 *
 * @param formatter formatter to format text to the textfield
 * @param dateTime property to contain currently selected dateTime
 * @see dateTimePicker
 */
class DateTimePicker(private val dateTime: Property<LocalDateTime>, private val formatter: DateTimeFormatter):
	Control() {
	// Popup containing the picker
	private val popup: DateTimePickerPopup = DateTimePickerPopup(dateTime)

	private lateinit var textField: TextField
	private lateinit var button: Button

	override fun createDefaultSkin(): Skin<*> {
		return object: SkinBase<DateTimePicker>(this) {
			override fun computeMaxWidth(
				height: Double, topInset: Double, rightInset: Double, bottomInset: Double, leftInset: Double
			): Double = super.computePrefWidth(height, topInset, rightInset, bottomInset, leftInset)

			override fun computeMaxHeight(
				width: Double, topInset: Double, rightInset: Double, bottomInset: Double, leftInset: Double
			): Double = super.computePrefHeight(width, topInset, rightInset, bottomInset, leftInset)
		}
	}

	init {
		hbox {
			style {
				borderColor += box(Color.DIMGREY)
				borderRadius += box(3.px)
				borderWidth += box(1.px)

				backgroundColor += Color.LIGHTGRAY
			}
			textField = textfield {
				prefHeight = 25.0
				isEditable = false
				isFocusTraversable = false
				text = formatter.format(dateTime.value)
				// transfer focus to the button
				focusedProperty().listen { focus ->
					if(focus)
						button.requestFocus()
				}
			}

			button = button {
				imageview(createFXImage("openPicker.svg", "/picker/dateTimePicker")) {
					fitHeight = 18.0
					fitWidth = 17.0
				}
				prefHeight = 24.5  // -1 because else border shadows outer border TODO check this
				// open popup on button press
				action {
					if(popup.isShowing) {
						popup.hide()
					} else {
						val x = this@hbox.localToScreen(0.0, 0.0).x
						val y = this@hbox.localToScreen(0.0, 0.0).y + height
						//val x = window.x + textField.localToScene(0.0, 0.0).x + textField.scene.x
						//val y = window.y + localToScene(0.0, 0.0).y + scene.y + height
						popup.show(parent, x, y)
					}
				}
			}
		}

		// update textField if dateTime changes
		dateTime.listen(runOnce = true) { new ->
			textField.text = formatter.format(new)
		}

		// hide popup if focus is lost
		popup.autoHideProperty().set(true)
	}

}
