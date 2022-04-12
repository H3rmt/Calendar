package picker

import calendar.Appointment
import frame.createFXImage
import javafx.beans.property.*
import javafx.collections.*
import javafx.event.*
import javafx.geometry.*
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.paint.*
import javafx.scene.text.*
import javafx.stage.*
import tornadofx.*

fun String.conditionalLowercase(bool: Boolean): String = if(bool) this.lowercase() else this

class AppointmentPickerPopup(appointment: Property<Appointment?>, private val appointments: List<Appointment>, save: () -> Unit): Popup() {
	private var replace: String? = null
	private val appointmentsList = FXCollections.observableArrayList(appointments)
	
	private fun filter(search: String) {
		val text = search.conditionalLowercase(true)
		appointmentsList.clear()
		for(app: Appointment in appointments)
			if(app.title.conditionalLowercase(true).contains(text) ||
				app.description.conditionalLowercase(true).contains(text) ||
				app.type.name.conditionalLowercase(true).contains(text)
			) appointmentsList.add(app)
	}
	
	init {
		content.add(
			vbox(spacing = 0.0, alignment = Pos.CENTER) {
				style(append = true) {
					maxWidth = 250.px
//					maxWidth = 450.px
					maxHeight = 200.px
					
					borderColor += box(Color.DIMGREY)
					//borderRadius += box(3.px)
					borderWidth += box(1.px)
					
					backgroundColor += Color.valueOf("#E9E9E9")
				}
				hbox(spacing = 5.0, alignment = Pos.CENTER) {
					style(append = true) {
						borderColor += box(c(0.75, 0.75, 0.75))
						borderStyle += BorderStrokeStyle.SOLID
						borderWidth += box(0.px, 0.px, 2.px, 0.px)
					}
					
					hbox(spacing = 3) {
						style {
							alignment = Pos.CENTER_RIGHT
							prefWidth = Int.MAX_VALUE.px
							
							padding = box(2.px)
						}
						imageview(createFXImage("select.svg")) {
							fitHeight = 14.0
							fitWidth = 14.0
						}
					}
					
					hbox(spacing = 3) {
						style {
							alignment = Pos.CENTER_RIGHT
							prefWidth = Int.MAX_VALUE.px
							
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
								replace = if((it.target as TextField).text != "")
									(it.target as TextField).text else null
								filter((it.target as TextField).text)
							}
						}
					}
				}
				scrollpane(fitToWidth = true, fitToHeight = true) {
					style {
						maxHeight = 100.px
						minHeight = 10.px
					}
					fun update() {
						vbox {
							for(app in appointmentsList) {
								hbox(spacing = 5.0, alignment = Pos.CENTER) {
									style(append = true) {
										borderColor += box(c(0.75, 0.75, 0.75))
										borderStyle += BorderStrokeStyle.DOTTED
										borderWidth += box(0.px, 0.px, 2.px, 0.px)
										
										padding = box(2.px, 0.px)
									}
									textflow {
										replace?.let {
											val strings = app.title.split(it.toRegex(RegexOption.IGNORE_CASE))
											for((index, text) in strings.withIndex()) {
												text(text)
												if(index != strings.size - 1)
													text(it) {
														style(append = true) {
															fontWeight = FontWeight.BOLD
														}
													}
											}
										} ?: text(app.title)
										style {
											alignment = Pos.CENTER
											prefWidth = Int.MAX_VALUE.px
											
											padding = box(2.px)
										}
									}
									textflow {
										replace?.let {
											val strings = app.description.split(it.toRegex(RegexOption.IGNORE_CASE))
											for((index, text) in strings.withIndex()) {
												text(text)
												if(index != strings.size - 1)
													text(it) {
														style(append = true) {
															fontWeight = FontWeight.BOLD
														}
													}
											}
										} ?: text(app.description)
										style {
											alignment = Pos.CENTER
											prefWidth = Int.MAX_VALUE.px
											
											padding = box(2.px)
										}
									}
									textflow {
										replace?.let {
											val strings = app.type.name.split(it.toRegex(RegexOption.IGNORE_CASE))
											for((index, text) in strings.withIndex()) {
												text(text)
												if(index != strings.size - 1)
													text(it) {
														style(append = true) {
															fontWeight = FontWeight.BOLD
														}
													}
											}
										} ?: text(app.type.name)
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
					}
					update()
					
					appointmentsList.addListener(ListChangeListener {
						update() // TODO smother repainting
					})
				}
			}
		)
	}
}