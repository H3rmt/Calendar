package picker.dropdownTogglePicker

import javafx.collections.ObservableList
import javafx.geometry.Pos
import javafx.scene.paint.Color
import javafx.stage.Popup
import listen
import listenAndRunOnce
import tornadofx.*

class DropdownTogglePickerPopup(toggles: ObservableList<DropdownToggle>, change: (DropdownToggle) -> Unit): Popup() {
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
				scrollpane(fitToWidth = true, fitToHeight = true) {
					fun update() {
						vbox(spacing = 4) {
							style(append = true) {
								padding = box(2.px)
							}
							for(toggle in toggles) {
								checkbox(toggle.name, toggle.selected)
								toggle.selected.listen {
									change(toggle)
								}
							}
						}
					}
					toggles.listenAndRunOnce {
						update()
					}
				}
			}
		)
	}
}
