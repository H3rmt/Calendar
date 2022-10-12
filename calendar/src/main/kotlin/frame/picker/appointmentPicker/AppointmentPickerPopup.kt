package frame.picker.appointmentPicker

import calendar.Appointment
import calendar.DBObservable
import frame.createFXImage
import frame.picker.dropdownTogglePicker.DropdownToggle
import frame.picker.dropdownTogglePicker.dropdownTogglePicker
import javafx.beans.property.*
import javafx.collections.*
import javafx.collections.FXCollections.*
import javafx.event.*
import javafx.geometry.*
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.paint.*
import javafx.scene.text.*
import javafx.stage.*
import logic.Configs
import logic.ObservableListListeners.listen
import logic.getConfig
import tornadofx.*

/**
 * Appointment picker popup
 *
 * @param appointment property to contain currently selected element
 * @param appointments possible appointments
 */
class AppointmentPickerPopup(
	private val appointment: Property<Appointment?>,
	private val appointments: ObservableList<Appointment>,
): Popup() {
	// null if all elements should get displayed, else all selected colums get filtered
	private var replace: String? = null

	// checkbox in filter dropdown to toggle filtering by titles
	private val titleSearchSelect = DropdownToggle(true, "title")

	// checkbox in filter dropdown to toggle filtering by descriptions
	private val descriptionSearchSelect = DropdownToggle(true, "description")

	// checkbox in filter dropdown to toggle filtering by types
	private val typeSearchSelect = DropdownToggle(true, "type")

	// list of appointments getting displayed
	private val appointmentsList = observableArrayList(appointments)

	init {
		// update appointmentsList (displayed list) when appointments change
		appointments.listen {
			appointmentsList.setAll(it)
		}
		filter()
	}

	private fun String.lowerConfigIgnore(): String =
		if(getConfig(Configs.IgnoreCaseForSearch)) this.lowercase() else this

	/**
	 * filter the appointments list and add appointments passing the filter
	 * conditions to appointmentsList
	 */
	private fun filter() {
		appointmentsList.clear()
		if(replace == null) {
			appointmentsList.addAll(appointments)
			return
		}
		val text = replace!!.lowerConfigIgnore()
		for(app: Appointment in appointments)
			@Suppress("ComplexCondition")
			if((titleSearchSelect.selected.value && app.title.value.lowerConfigIgnore().contains(text)) ||
				(descriptionSearchSelect.selected.value && app.description.value.lowerConfigIgnore().contains(text)) ||
				(typeSearchSelect.selected.value && app.type.value.name.value.lowerConfigIgnore().contains(text))
			)
				appointmentsList.add(app)
	}

	init {
		content.add(
			vbox(spacing = 0.0, alignment = Pos.CENTER) {
				style(append = true) {
					maxWidth = 250.px
					minWidth = 140.px
					maxHeight = 200.px

					borderColor += box(Color.DIMGREY)
					borderWidth += box(1.px)

					backgroundColor += Color.valueOf("#E9E9E9")
				}

				// header bar
				hbox(spacing = 10.0, alignment = Pos.CENTER_RIGHT) {
					style(append = true) {
						borderColor += box(c(0.75, 0.75, 0.75))
						borderStyle += BorderStrokeStyle.SOLID
						borderWidth += box(0.px, 0.px, 2.px, 0.px)
					}

					hbox(spacing = 3, alignment = Pos.CENTER_LEFT) {
						style {
							padding = box(2.px)
						}
						dropdownTogglePicker("filter",
							observableArrayList(titleSearchSelect, descriptionSearchSelect, typeSearchSelect), {
								filter()
							}
						)
					}

					hbox(spacing = 3, alignment = Pos.CENTER_RIGHT) {
						style {
							padding = box(2.px)
						}
						imageview(createFXImage("search.svg", "picker/appointmentPicker")) {
							fitHeight = 14.0
							fitWidth = 14.0
						}
						textfield {
							style {
								padding = box(2.px, 4.px)
								maxWidth = 50.px
							}
							// update replace and filter appointmentsList
							onKeyTyped = EventHandler {
								replace = if((it.target as TextField).text != "")
									(it.target as TextField).text else null
								filter()
							}
						}
					}
				}

				// appointemnt List display
				scrollpane(fitToWidth = true, fitToHeight = true) {
					style {
						maxHeight = 100.px
						minHeight = 10.px
					}
					vbox {
						val update: (List<Appointment>) -> Unit = { list: List<Appointment> ->
							clear()
							for(app in list) {
								appointmentDisplay(this, app)
							}
						}
						appointmentsList.listen(update, runOnce = true)
					}
				}
			})
	}

	/**
	 * creates a row for an Appointment
	 *
	 * @param vBox ref to parent element
	 * @param app Appointment
	 */
	private fun appointmentDisplay(vBox: VBox, app: Appointment) {
		vBox.hbox(spacing = 5.0, alignment = Pos.CENTER) {
			style(append = true) {
				borderColor += box(c(0.75, 0.75, 0.75))
				borderStyle += BorderStrokeStyle.DOTTED
				borderWidth += box(0.px, 0.px, 2.px, 0.px)

				padding = box(2.px, 0.px)
			}
            // title cell
			textflow {
				textfieldContent(app.title, this, titleSearchSelect.selected)
				style {
					alignment = Pos.CENTER
					prefWidth = Int.MAX_VALUE.px

					padding = box(2.px)
				}
			}
            // description cell
			textflow {
				textfieldContent(app.description, this, descriptionSearchSelect.selected)
				style {
					alignment = Pos.CENTER
					prefWidth = Int.MAX_VALUE.px

					padding = box(2.px)
				}
			}
            // type cell
			textflow {
				textfieldContent(app.type.value.name, this, typeSearchSelect.selected)
				style {
					alignment = Pos.CENTER
					prefWidth = Int.MAX_VALUE.px

					padding = box(2.px)
				}
			}
			onMouseClicked = EventHandler {
				appointment.value = app
				this@AppointmentPickerPopup.hide()
			}
		}
	}

	/**
	 * generates text content for a TextFlow showing a cel
	 *
	 * makes replace bold in text
	 *
	 * @param str string to show in call
	 * @param textFlow ref to parent
	 * @param selected boolean if searching on this row is enabled
	 */
	private fun textfieldContent(str: DBObservable<String>, textFlow: TextFlow, selected: BooleanProperty) {
		if(replace != null && selected.value) {
			val strings = str.value.split(replace!!.toRegex(RegexOption.IGNORE_CASE))
			// returns list of strings arround replace
			// replace = foo;  str.value = "Te foo test t f foogeometry"
			// strings = ["Te ", " test t f ", "geometry"]
			for((index, text) in strings.withIndex()) {
				textFlow.text(text)
				// dont add replace on last element
				if(index != strings.size - 1)
					textFlow.text(replace) {
						style(append = true) {
							fontWeight = FontWeight.BOLD
						}
					}
			}
		} else textFlow.text(str)
	}
}
