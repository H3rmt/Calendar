package picker.appointmentPicker

import calendar.Appointment
import frame.createFXImage
import javafx.beans.property.Property
import javafx.collections.FXCollections.observableArrayList
import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.control.TextField
import javafx.scene.layout.BorderStrokeStyle
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import javafx.stage.Popup
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
	private val appointmentsList = observableArrayList(appointments) // TODO add new appointments to this list
	private val searchColumns = observableArrayList(
		DropdownToggle(true, "title"), DropdownToggle(true, "description"), DropdownToggle(true, "type")
	)
	
	private fun String.conditionalLowercase(bool: Boolean): String = if(bool) this.lowercase() else this
	
	private fun filter() {
		if(replace != null) {
			val text = replace!!.conditionalLowercase(getConfig(Configs.IgnoreCaseForSearch))
			appointmentsList.clear()
			for(app: Appointment in appointments)
				@Suppress("ComplexCondition")
				if((searchColumns.find { it.name == "title" }!!.selected.value &&
							  app.title.value.conditionalLowercase(getConfig(Configs.IgnoreCaseForSearch))
								  .contains(text)) || (searchColumns.find { it.name == "description" }!!.selected.value &&
							  app.description.value.conditionalLowercase(getConfig(Configs.IgnoreCaseForSearch))
								  .contains(text)) || (searchColumns.find { it.name == "type" }!!.selected.value &&
							  app.type.value.name.value.conditionalLowercase(getConfig(Configs.IgnoreCaseForSearch))
								  .contains(text))
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
					dropdownTogglePicker("filter", searchColumns, {
						filter()
					})
				}
				
				hbox(spacing = 3, alignment = Pos.CENTER_RIGHT) {
					style {
						padding = box(2.px)
					}
					imageview(createFXImage("search.svg")) {
						fitHeight = 14.0
						fitWidth = 14.0
					}
					textfield {
						style {
							padding = box(2.px, 4.px)
							maxWidth = 50.px
						}
						onKeyTyped = EventHandler {
							replace = if((it.target as TextField).text != "") (it.target as TextField).text else null
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
									if(replace != null && searchColumns.find { it.name == "title" }!!.selected.value) {
										val strings = app.title.value.split(replace!!.toRegex(RegexOption.IGNORE_CASE))
										for((index, text) in strings.withIndex()) {
											text(text)
											if(index != strings.size - 1) text(replace) {
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
									if(replace != null && searchColumns.find { it.name == "description" }!!.selected.value) {
										val strings = app.description.value.split(replace!!.toRegex(RegexOption.IGNORE_CASE))
										for((index, text) in strings.withIndex()) { // TODO bind here same as when no filter (no .value)
											text(text)
											if(index != strings.size - 1) text(replace) {
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
									if(replace != null && searchColumns.find { it.name == "type" }!!.selected.value) {
										val strings = app.type.value.name.value.split(replace!!.toRegex(RegexOption.IGNORE_CASE))
										for((index, text) in strings.withIndex()) {
											text(text)
											if(index != strings.size - 1) text(replace) {
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
