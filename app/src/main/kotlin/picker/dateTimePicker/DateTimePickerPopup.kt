package picker.dateTimePicker

import javafx.beans.property.*
import javafx.event.*
import javafx.geometry.*
import javafx.scene.control.*
import javafx.scene.paint.*
import javafx.stage.*
import tornadofx.*
import java.time.LocalDate

class DateTimePickerPopup(dateProperty: Property<LocalDate>, hourProperty: IntegerProperty, minuteProperty: IntegerProperty, save: () -> Unit): Popup() {
	
	private lateinit var datepicker: DatePicker
	private lateinit var timepicker: TimePicker
	
	init {
		content.add(vbox(spacing = 5.0) {
			style {
				borderColor += box(Color.DIMGREY)
				//borderRadius += box(3.px)
				borderWidth += box(1.px)
				
				backgroundColor += Color.valueOf("#E9E9E9")
			}
			datepicker = datepicker(dateProperty) { }
			timepicker = timepicker(hourProperty, minuteProperty, save) { }
		})
	}
}

class TimePicker(hourProperty: IntegerProperty, minuteProperty: IntegerProperty, save: () -> Unit): Control() {
	
	override fun createDefaultSkin(): Skin<*> {
		return object: SkinBase<TimePicker>(this) {
		}
	}
	
	init {
		vbox(spacing = 5.0) {
			//prefWidth = 40.0
			style(append = true) {
				padding = box(0.px, 3.px, 3.px, 3.px)
			}
			
			hbox(spacing = 3.0, alignment = Pos.CENTER) {
				spinner(min = 0, max = 23, amountToStepBy = 1, enableScroll = true, property = hourProperty) {
					prefWidth = 65.0
					isEditable = true
				}
				text(" : ")
				spinner(min = 0, max = 59, amountToStepBy = 1, enableScroll = true, property = minuteProperty) {
					prefWidth = 65.0
					isEditable = true
				}
			}
			hbox(spacing = 1.0, alignment = Pos.CENTER) {
				slider(min = 0, max = 23, orientation = Orientation.HORIZONTAL) {
					prefWidth = 55.0
					valueProperty().bindBidirectional(hourProperty)
				}
				button("OK") {
					action {
						save()
					}
				}
				slider(min = 0, max = 59, orientation = Orientation.HORIZONTAL) {
					prefWidth = 55.0
					valueProperty().bindBidirectional(minuteProperty)
				}
			}
		}
	}
}

fun EventTarget.timepicker(hourProperty: IntegerProperty, minuteProperty: IntegerProperty, save: () -> Unit, op: TimePicker.() -> Unit = {}): TimePicker =
	TimePicker(hourProperty, minuteProperty, save).attachTo(this, op).apply {
		op(this)
	}