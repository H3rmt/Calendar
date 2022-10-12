package frame.picker.appointmentPicker

import calendar.Appointment
import frame.createFXImage
import javafx.beans.property.*
import javafx.collections.*
import javafx.event.*
import javafx.scene.control.*
import javafx.scene.paint.*
import logic.ObservableValueListeners.listen
import tornadofx.*


/**
 * Extension function for creating an [AppointmentPicker] with observable
 * list of appointments to let the user choose from
 *
 * @param appointments possible appointments
 * @param appointment property to contain currently selected element
 * @see AppointmentPicker
 * @see Appointment
 */
fun EventTarget.appointmentPicker(
	appointments: ObservableList<Appointment>,
	appointment: Property<Appointment?>,
	op: AppointmentPicker.() -> Unit = {}
): AppointmentPicker {
	val picker = AppointmentPicker(appointment, appointments)
	return opcr(this, picker, op)
}


/**
 * class representing the Appointment picker deriving from the Control Node
 *
 * @param appointment property to contain currently selected element
 * @param appointments possible appointments
 * @see appointmentPicker
 */
class AppointmentPicker(appointment: Property<Appointment?>, appointments: ObservableList<Appointment>): Control() {
	// Popup containing the picker
	private val popup: AppointmentPickerPopup = AppointmentPickerPopup(appointment, appointments)

	private lateinit var textField: TextField
	private lateinit var button: Button

	override fun createDefaultSkin(): Skin<*> {
		return object: SkinBase<AppointmentPicker>(this) {
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
				// transfer focus to the button
				focusedProperty().listen { focus ->
					if(focus)
						button.requestFocus()
				}
			}

			button = button {
				imageview(createFXImage("openPicker.svg", "/picker/appointmentPicker")) {
					fitHeight = 18.0
					fitWidth = 17.0
				}
				prefHeight = 22.0
				// open popup on button press
				action {
					if(popup.isShowing) {
						popup.hide()
					} else {
						val x = this@hbox.localToScreen(0.0, 0.0).x
						val y = this@hbox.localToScreen(0.0, 0.0).y + height
						popup.show(parent, x, y)
					}
				}
			}
		}

		// update textfield content when appointmentProperty is updated and add listeners
		// to net app to update if appointment changes
		appointment.listen(runOnce = true) { app: Appointment? ->
			val update = {
//				log("UPDATE ! $app", LogType.WARNING)
				textField.text = "${app?.title?.value} ${app?.description?.value}"
			}
			app?.title?.listen(update)
			app?.title?.listen(update)
			app?.description?.listen(update)
			if(app != null)
				update()
		}

        // hide popup if focus is lost
		popup.autoHideProperty().set(true)
	}

}
