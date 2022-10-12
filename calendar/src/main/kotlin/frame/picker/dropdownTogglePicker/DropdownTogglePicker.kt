package frame.picker.dropdownTogglePicker

import javafx.beans.property.*
import javafx.collections.*
import javafx.event.*
import javafx.scene.control.*
import javafx.scene.paint.*
import tornadofx.*

/**
 * Extension function for creating an [DropdownTogglePicker] with
 * observable list of toggle to switch on and off
 *
 * @param name name for dropdown
 * @param toggles observable List of toggles
 * @see DropdownTogglePicker
 * @see DropdownToggle
 */
fun EventTarget.dropdownTogglePicker(
	name: String,
	toggles: ObservableList<DropdownToggle>,
	op: DropdownTogglePicker.() -> Unit = {}
): DropdownTogglePicker {
	val picker = DropdownTogglePicker(name, toggles)
	return opcr(this, picker, op)
}


/**
 * class representing the DropdownToggle picker deriving from the Control
 * Node
 *
 * @param name name for dropdown
 * @param toggles observable List of toggles
 * @see dropdownTogglePicker
 */
class DropdownTogglePicker(name: String, toggles: ObservableList<DropdownToggle>): Control() {

	// Popup containing the picker
	private val popup: DropdownTogglePickerPopup = DropdownTogglePickerPopup(toggles)

	private lateinit var button: Button

	override fun createDefaultSkin(): Skin<*> {
		return object: SkinBase<DropdownTogglePicker>(this) {
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
			button = button("$name ⯆") {
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

		// hide popup if focus is lost
		popup.autoHideProperty().set(true)
	}

}

data class DropdownToggle(val selected: BooleanProperty, val name: String) {
	constructor(selected: Boolean, name: String): this(selected.toProperty(), name)
}
