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
				
				backgroundColor += Color.LIGHTGRAY
			}
			datepicker = datepicker(dateProperty) { }
			timepicker = timepicker(hourProperty, minuteProperty) { }
			borderpane {
				padding = insets(5)
				center = button("OK") {
					action {
						save()
					}
				}
			}
		})
	}
}

class TimePicker(hourProperty: IntegerProperty, minuteProperty: IntegerProperty): Control() {
	
	override fun createDefaultSkin(): Skin<*> {
		return object: SkinBase<TimePicker>(this) {
		}
	}
	
	init {
		hbox(spacing = 3.0, alignment = Pos.CENTER) {
			vbox(spacing = 1.0) {
				spinner(min = 0, max = 23, amountToStepBy = 1, enableScroll = true, property = hourProperty) {
					isEditable = true
				}
				slider(min = 0, max = 23, orientation = Orientation.HORIZONTAL) {
					valueProperty().bindBidirectional(hourProperty)
				}
				prefWidth = 60.0
			}
			text(" : ") {
			
			}
			vbox(spacing = 1.0) {
				spinner(min = 0, max = 59, amountToStepBy = 1, enableScroll = true, property = minuteProperty) {
					isEditable = true
				}
				slider(min = 0, max = 59, orientation = Orientation.HORIZONTAL) {
					valueProperty().bindBidirectional(minuteProperty)
				}
				prefWidth = 60.0
			}
		}
	}
}

fun EventTarget.timepicker(hourProperty: IntegerProperty, minuteProperty: IntegerProperty, op: TimePicker.() -> Unit = {}): TimePicker =
	TimePicker(hourProperty, minuteProperty).attachTo(this, op).apply {
		op(this)
	}