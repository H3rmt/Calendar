package picker

import println as printlnm
import javafx.beans.property.*
import javafx.collections.*
import javafx.event.*
import javafx.scene.control.*
import javafx.scene.paint.*
import javafx.stage.*
import tornadofx.*
import kotlin.random.Random

fun main() {
	launch<TestWindoww>()
}

class TestWindoww: App(TestMainVieww::class) {
	override fun start(stage: Stage) {
		stage.height = 150.0
		stage.width = 300.0
		super.start(stage)
	}
}

class TestMainVieww: View("Test") {
	override val root = borderpane {
		prefWidth = Double.MAX_VALUE
		val list = FXCollections.observableArrayList<DropdownToggle>()
		center = dropdownTogglePicker(list) {
		
		}
//		left = combobox(values = list)
		right = button {
			action {
				list.add(DropdownToggle(false, "test tes test" + Random.nextInt(1, 1124432524) + Random.nextInt(1, 1124432524)))
			}
		}
	}
}


fun EventTarget.dropdownTogglePicker(
	list: ObservableList<DropdownToggle>,
	op: DropdownTogglePicker.() -> Unit = {}
): DropdownTogglePicker {
	val picker = DropdownTogglePicker(list) {
		printlnm("update", it)
	}
	return opcr(this, picker, op)
}


class DropdownTogglePicker(toggles: ObservableList<DropdownToggle>, change: (DropdownToggle) -> Unit): Control() {
	
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
			button = button("select ⯆") {
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