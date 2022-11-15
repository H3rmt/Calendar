package ui.picker.dropdownTogglePicker

import javafx.collections.*
import javafx.geometry.*
import javafx.scene.paint.*
import javafx.stage.*
import logic.ObservableListListeners.listen
import tornadofx.*

class DropdownTogglePickerPopup(toggles: ObservableList<DropdownToggle>): Popup() {
	init {
		content.add(
			vbox(spacing = 0.0, alignment = Pos.CENTER) {
				style(append = true) {
					maxWidth = 170.px
					minWidth = 100.px
					maxHeight = 90.px
					minHeight = 10.px

					borderColor += box(Color.DIMGREY)
					borderWidth += box(1.px)

					backgroundColor += Color.valueOf("#E9E9E9")
				}

				// list with toggles
				scrollpane(fitToWidth = true, fitToHeight = true) {
					val update: (List<DropdownToggle>) -> Unit = { _: List<DropdownToggle> ->
						vbox(spacing = 4) {
							style(append = true) {
								padding = box(2.px)
							}
							for((selected, name) in toggles) {
								checkbox(name, selected)
							}
						}
					}
					toggles.listen(update, runOnce = true)
				}
			}
		)
	}
}
