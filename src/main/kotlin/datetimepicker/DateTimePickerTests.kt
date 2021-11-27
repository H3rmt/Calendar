import frame.createFXImage
import javafx.beans.property.*
import javafx.event.*
import javafx.scene.control.*
import javafx.scene.paint.*
import javafx.stage.*
import tornadofx.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle



fun main() {
	launch<TestWindow>()
}

class TestWindow: App(TestMainView::class) {
	override fun start(stage: Stage) {
		stage.height = 150.0
		stage.width = 300.0
		super.start(stage)
	}
}

class TestMainView: View("Test") {
	override val root = borderpane {
		prefWidth = Double.MAX_VALUE
		center = dateTimePicker { dateTimeProperty.lglisten() }
		left = button("useless")
	}
}


fun EventTarget.dateTimePicker(
	dateTime: Property<LocalDateTime> = LocalDateTime.now().toProperty(),
	formatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT),
	op: DateTimePicker.() -> Unit = {}
): DateTimePicker {
	val picker = DateTimePicker(dateTime.value, formatter)
	picker.dateTimeProperty.bindBidirectional(dateTime)
	return opcr(this, picker, op)
}

open class DateTimePicker(dateTime: LocalDateTime, private val formatter: DateTimeFormatter): Control() {
	
	// this property only gets updated if the OK button is pressed
	val dateTimeProperty: Property<LocalDateTime> = SimpleObjectProperty(dateTime)
	
	// fake Property that generate its values from other props when it gets accessed, and updates the other props if it gets changed
	private val timeProperty: Property<LocalTime> = object: SimpleObjectProperty<LocalTime>() {
		override fun getValue(): LocalTime = LocalTime.of(hourProperty.value, minuteProperty.value)
		
		override fun setValue(v: LocalTime?) {
			minuteProperty.value = v?.minute
			hourProperty.value = v?.hour
		}
	}
	
	
	private val dateProperty: Property<LocalDate> = dateTime.toLocalDate().toProperty()
	protected val minuteProperty: IntegerProperty = dateTime.minute.toProperty()
	protected val hourProperty: IntegerProperty = dateTime.hour.toProperty()
	
	
	private val popup: DateTimePickerPopup = DateTimePickerPopup(dateProperty, hourProperty, minuteProperty) {
		button.fire()
		dateTimeProperty.value = LocalDateTime.of(dateProperty.value, timeProperty.value)
	}
	
	private lateinit var textField: TextField
	private lateinit var button: Button
	
	override fun createDefaultSkin(): Skin<*> {
		return object: SkinBase<DateTimePicker>(this) {
			override fun computeMaxWidth(height: Double, topInset: Double, rightInset: Double, bottomInset: Double, leftInset: Double): Double =
				super.computePrefWidth(height, topInset, rightInset, bottomInset, leftInset)
			
			override fun computeMaxHeight(width: Double, topInset: Double, rightInset: Double, bottomInset: Double, leftInset: Double): Double =
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
				text = formatter.format(dateTimeProperty.value)
				focusedProperty().addListener { _, _, focus ->
					if(focus) button.requestFocus()
				}
			}
			
			button = button {
				imageview(createFXImage("calendar.png"))
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
		
		dateTimeProperty.addListener { _, _, new: LocalDateTime ->
			textField.text = formatter.format(new)
		}
		
		popup.autoHideProperty().set(true)
	}
	
}
