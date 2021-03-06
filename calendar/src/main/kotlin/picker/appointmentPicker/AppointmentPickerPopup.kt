package picker.appointmentPicker

import calendar.Appointment
import frame.createFXImage
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
import logic.getConfig
import logic.listen
import picker.dropdownTogglePicker.DropdownToggle
import picker.dropdownTogglePicker.dropdownTogglePicker
import tornadofx.*

class AppointmentPickerPopup(
	appointment: Property<Appointment?>,
	private val appointments: ObservableList<Appointment>, save: () -> Unit
): Popup() {
	private var replace: String? = null
	private val titleSearchSelect = DropdownToggle(true, "title")
	private val descriptionSearchSelect = DropdownToggle(true, "description")
	private val typeSearchSelect = DropdownToggle(true, "type")

	private val appointmentsList = observableArrayList(appointments)

	init {
		appointments.listen {
			appointmentsList.setAll(it)
		}
		filter()
	}

	private fun String.lowerConfigIgnore(): String =
		if(getConfig(Configs.IgnoreCaseForSearch)) this.lowercase() else this

	private fun filter() {
		if(replace != null) {
			val text = replace!!.lowerConfigIgnore()
			appointmentsList.clear()
			for(app: Appointment in appointments)
				@Suppress("ComplexCondition")
				if((titleSearchSelect.selected.value && app.title.value.lowerConfigIgnore().contains(text)) ||
					(descriptionSearchSelect.selected.value && app.description.value.lowerConfigIgnore().contains(text)) ||
					(typeSearchSelect.selected.value && app.type.value.name.value.lowerConfigIgnore().contains(text))
				) appointmentsList.add(app)
		} else {
			appointmentsList.clear()
			appointmentsList.addAll(appointments)
		}
	}

	init {
		content.add(vbox(spacing = 0.0, alignment = Pos.CENTER) {
			style(append = true) {
				maxWidth = 250.px
				minWidth = 140.px
				maxHeight = 200.px

				borderColor += box(Color.DIMGREY)
				borderWidth += box(1.px)

				backgroundColor += Color.valueOf("#E9E9E9")
			}
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
						onKeyTyped = EventHandler {
							replace = if((it.target as TextField).text != "")
								(it.target as TextField).text else null
							filter()
						}
					}
				}
			}
			scrollpane(fitToWidth = true, fitToHeight = true) {
				style {
					maxHeight = 100.px
					minHeight = 10.px
				}
				vbox {
					val update: (List<Appointment>) -> Unit = { list: List<Appointment> ->
						clear()
						for(app in list) {
							hbox(spacing = 5.0, alignment = Pos.CENTER) {
								style(append = true) {
									borderColor += box(c(0.75, 0.75, 0.75))
									borderStyle += BorderStrokeStyle.DOTTED
									borderWidth += box(0.px, 0.px, 2.px, 0.px)

									padding = box(2.px, 0.px)
								}
								textflow {
									if(replace != null && titleSearchSelect.selected.value) {
										val strings = app.title.value.split(replace!!.toRegex(RegexOption.IGNORE_CASE))
										for((index, text) in strings.withIndex()) {
											text(text)
											if(index != strings.size - 1)
												text(replace) {
													style(append = true) {
														fontWeight = FontWeight.BOLD
													}
												}
										}
									} else text(app.title)
									style {
										alignment = Pos.CENTER
										prefWidth = Int.MAX_VALUE.px

										padding = box(2.px)
									}
								}
								textflow {
									if(replace != null && descriptionSearchSelect.selected.value) {
										val strings = app.description.value.split(replace!!.toRegex(RegexOption.IGNORE_CASE))
										for((index, text) in strings.withIndex()) { // TODO bind here same as when no filter (no .value)
											text(text)
											if(index != strings.size - 1)
												text(replace) {
													style(append = true) {
														fontWeight = FontWeight.BOLD
													}
												}
										}
									} else text(app.description)
									style {
										alignment = Pos.CENTER
										prefWidth = Int.MAX_VALUE.px

										padding = box(2.px)
									}
								}
								textflow {
									if(replace != null && typeSearchSelect.selected.value) {
										val strings = app.type.value.name.value.split(replace!!.toRegex(RegexOption.IGNORE_CASE))
										for((index, text) in strings.withIndex()) {
											text(text)
											if(index != strings.size - 1)
												text(replace) {
													style(append = true) {
														fontWeight = FontWeight.BOLD
													}
												}
										}
									} else text(app.type.value.name)
									style {
										alignment = Pos.CENTER
										prefWidth = Int.MAX_VALUE.px

										padding = box(2.px)
									}
								}
								onMouseClicked = EventHandler {
									appointment.value = app
									save()
								}
							}
						}
					}
					appointmentsList.listen(update, runOnce = true)
				}
			}
		})
	}
}
