package picker.dropdownTogglePicker

import javafx.beans.property.*
import javafx.collections.*
import javafx.event.*
import javafx.scene.control.*
import javafx.scene.paint.*
import tornadofx.*


fun EventTarget.dropdownTogglePicker(
	name: String,
	list: ObservableList<DropdownToggle>,
	change: ((DropdownToggle) -> Unit)?,
	op: DropdownTogglePicker.() -> Unit = {}
): DropdownTogglePicker {
	val picker = DropdownTogglePicker(name, list, change ?: {})
	return opcr(this, picker, op)
}


class DropdownTogglePicker(name: String, toggles: ObservableList<DropdownToggle>, change: (DropdownToggle) -> Unit): Control() {
	
	private val popup: DropdownTogglePickerPopup = DropdownTogglePickerPopup(toggles, change)
	
	private lateinit var button: Button
	
	override fun createDefaultSkin(): Skin<*> {
		return object: SkinBase<DropdownTogglePicker>(this) {
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
		
		popup.autoHideProperty().set(true)
	}
	
}

data class DropdownToggle(val selected: BooleanProperty, val name: String) {
	constructor(selected: Boolean, name: String): this(selected.toProperty(), name)
}