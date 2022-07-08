package picker.dateTimePicker

import frame.createFXImage
import javafx.beans.property.*
import javafx.event.*
import javafx.scene.control.*
import javafx.scene.paint.*
import logic.listen
import tornadofx.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


fun EventTarget.dateTimePicker(
	dateTime: Property<LocalDateTime> = LocalDateTime.now().toProperty(),
	formatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT),
	op: DateTimePicker.() -> Unit = {}
): DateTimePicker {
	val picker = DateTimePicker(dateTime, formatter)
	return opcr(this, picker, op)
}

class DateTimePicker(private val dateTime: Property<LocalDateTime>, private val formatter: DateTimeFormatter): Control() {
	private val dateProperty: Property<LocalDate> = dateTime.value.toLocalDate().toProperty()
	private val minuteProperty: IntegerProperty = dateTime.value.minute.toProperty()
	private val hourProperty: IntegerProperty = dateTime.value.hour.toProperty()


	private val popup: DateTimePickerPopup = DateTimePickerPopup(dateProperty, hourProperty, minuteProperty) {
		dateTime.value = LocalDateTime.of(dateProperty.value, LocalTime.of(hourProperty.value, minuteProperty.value))
		button.fire()
	}

	private lateinit var textField: TextField
	private lateinit var button: Button

	override fun createDefaultSkin(): Skin<*> {
		return object: SkinBase<DateTimePicker>(this) {
			override fun computeMaxWidth(
				height: Double,
				topInset: Double,
				rightInset: Double,
				bottomInset: Double,
				leftInset: Double
			): Double =
				super.computePrefWidth(height, topInset, rightInset, bottomInset, leftInset)

			override fun computeMaxHeight(
				width: Double,
				topInset: Double,
				rightInset: Double,
				bottomInset: Double,
				leftInset: Double
			): Double =
				super.computePrefHeight(width, topInset, rightInset, bottomInset, leftInset)
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
				focusedProperty().listen { focus ->
					if(focus)
						button.requestFocus()
				}
			}

			button = button {
				imageview(createFXImage("openPicker.svg", "picker/dateTimePicker")) {
					fitHeight = 18.0
					fitWidth = 17.0
				}
				prefHeight = 24.5  // -1 because else border shadows outer border
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

		dateTime.listen(runOnce = true) { new ->
			textField.text = formatter.format(new)
		}

		popup.autoHideProperty().set(true)
	}

}
