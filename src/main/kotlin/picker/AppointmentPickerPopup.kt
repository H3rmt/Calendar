package picker

import calendar.Appointment
import javafx.beans.property.*
import javafx.geometry.*
import javafx.scene.layout.*
import javafx.scene.paint.*
import javafx.stage.*
import tornadofx.*


class AppointmentPickerPopup(appointment: Property<Appointment?>, appointments: List<Appointment>, save: () -> Unit): Popup() {
	init {
		content.add(
			vbox(spacing = 0.0, alignment = Pos.CENTER) {
				style(append = true) {
					maxWidth = 250.px
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
					label("Title") {
						style {
							alignment = Pos.CENTER
							prefWidth = Int.MAX_VALUE.px
							
							padding = box(2.px)
						}
					}
					label("Description") {
						style {
							alignment = Pos.CENTER
							prefWidth = Int.MAX_VALUE.px
							
							padding = box(2.px)
						}
					}
				}
				scrollpane(fitToWidth = true, fitToHeight = true) {
					style {
						maxHeight = 100.px
					}
					vbox {
						for((index, app) in appointments.withIndex()) {
							hbox(spacing = 5.0, alignment = Pos.CENTER) {
								style(append = true) {
									borderColor += box(c(0.75, 0.75, 0.75))
									borderStyle += BorderStrokeStyle.DOTTED
									borderWidth += box(0.px, 0.px, 2.px, 0.px)
									
									padding = box(2.px, 0.px)
								}
								label(app.title) {
									style {
										alignment = Pos.CENTER
										prefWidth = Int.MAX_VALUE.px
										
										padding = box(2.px)
									}
								}
								label(app.description) {
									style {
										alignment = Pos.CENTER
										prefWidth = Int.MAX_VALUE.px
										
										padding = box(2.px)
									}
								}
							}
						}
					}
				}
			})
	}
}