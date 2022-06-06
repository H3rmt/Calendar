package picker.appointmentPicker

import calendar.Appointment
import frame.createFXImage
import javafx.beans.property.Property
import javafx.beans.property.SimpleObjectProperty
import javafx.event.EventTarget
import javafx.scene.control.*
import javafx.scene.paint.Color
import logic.listen
import tornadofx.*


fun EventTarget.appointmentPicker(
	appointments: List<Appointment>, // TODO make this observable
	appointment: Property<Appointment?> = SimpleObjectProperty<Appointment?>(null), op: AppointmentPicker.() -> Unit = {}
): AppointmentPicker {
	val picker = AppointmentPicker(appointment.value, appointments)
	picker.appointmentProperty.bindBidirectional(appointment)
	return opcr(this, picker, op)
}

class AppointmentPicker(appointment: Appointment?, appointments: List<Appointment>): Control() {
	
	// this property only gets updated if the OK button is pressed
	val appointmentProperty: Property<Appointment?> = SimpleObjectProperty(appointment)
	
	
	private val popup: AppointmentPickerPopup = AppointmentPickerPopup(appointmentProperty, appointments) {
		button.fire()
	}
	
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
				focusedProperty().listen { focus ->
					if(focus)
						button.requestFocus()
				}
			}
			
			button = button {
				imageview(createFXImage("appointmentchooser.svg")) {
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
						popup.show(parent, x, y)
					}
				}
			}
		}
		
		appointmentProperty.listen(runOnce = true) {
			textField.text = "${it?.title ?: ""} ${it?.description ?: ""}"
		}
		
		popup.autoHideProperty().set(true)
	}
	
}
