package frame.popup

import calendar.Appointment
import calendar.Timing
import calendar.Timing.toUTCEpochMinute
import calendar.getTypes
import frame.styles.GlobalStyles
import javafx.beans.property.*
import javafx.scene.layout.*
import javafx.scene.paint.*
import javafx.scene.text.*
import javafx.stage.*
import logic.Language
import logic.translate
import picker.dateTimePicker.dateTimePicker
import tornadofx.*
import java.time.LocalDateTime


class AppointmentPopup: Fragment() {
	override val scope = super.scope as ItemsScope
	
	private var appointment: Appointment? = scope.appointment
	
	// do not bind directly, instead copy values into new Observables, to only save an updateAppointment()
	private var start: Property<LocalDateTime> = (appointment?.start?.let { Timing.fromUTCEpochMinuteToLocalDateTime(it) } ?: scope.start).toProperty()
	private var end: Property<LocalDateTime> = (appointment?.let { Timing.fromUTCEpochMinuteToLocalDateTime(it.start + it.duration) } ?: scope.end).toProperty()
	private var appointmentTitle: Property<String> = (appointment?.title ?: "").toProperty()
	private var description: Property<String> = (appointment?.description ?: "").toProperty()
	private var type: Property<String> = (appointment?.type?.name ?: "").toProperty()
	
	private var onSave: (Appointment) -> Unit = scope.save
	
	private var error: Property<String> = "".toProperty()
	
	private var windowTitle: String = scope.title
	private var saveTitle: String = scope.saveTitle
	
	private fun updateAppointment() {
		appointment?.let { app ->
			app.title = appointmentTitle.value
			app.description = description.value
			app.start = start.value.toUTCEpochMinute()
			app.duration = end.value.toUTCEpochMinute() - start.value.toUTCEpochMinute()
			app.type = getTypes().find { it.name == type.value }!!
		}
	}
	
	private fun createAppointment(): Appointment = Appointment.new(
		start.value.toUTCEpochMinute(),
		end.value.toUTCEpochMinute() - start.value.toUTCEpochMinute(),
		appointmentTitle.value,
		description.value,
		getTypes().find { it.name == type.value }!!,
		false
	)
	
	@Suppress("ReturnCount")
	private fun checkAppointment(): String? {
		if(type.value == "") {
			return "missing type".translate(Language.TranslationTypes.AppointmentPopup)
		} else if(appointmentTitle.value.isEmpty()) {
			return "missing title".translate(Language.TranslationTypes.AppointmentPopup)
		} else if(end.value.toUTCEpochMinute() < start.value.toUTCEpochMinute()) {
			return "start must be before end".translate(Language.TranslationTypes.AppointmentPopup)
		}
		return null
	}
	
	override fun onBeforeShow() {
		modalStage?.height = 320.0
		modalStage?.width = 400.0
	}
	
	override val root = form {
		addClass(GlobalStyles.background_)
		fieldset(windowTitle) {
			addClass(GlobalStyles.maxHeight_)
			field("Type") {
				combobox(values = getTypes().map { it.name }, property = type)
			}
			field("start to end".translate(Language.TranslationTypes.AppointmentPopup)) {
				dateTimePicker(dateTime = start)
				dateTimePicker(dateTime = end)
			}
			field("title".translate(Language.TranslationTypes.AppointmentPopup)) {
				textfield(appointmentTitle)
			}
			field("description".translate(Language.TranslationTypes.AppointmentPopup)) {
				addClass(GlobalStyles.maxHeight_)
				style(append = true) {
					minHeight = 60.px
					padding = box(0.px, 0.px, 20.px, 0.px)
				}
				textarea(description) {
					addClass(GlobalStyles.maxHeight_)
				}
			}
			
			buttonbar {
				textfield(error) {
					style(append = true) {
						backgroundColor += Color.TRANSPARENT
						borderStyle += BorderStrokeStyle.NONE
						textFill = Color.RED
						fontSize = 120.percent
						fontWeight = FontWeight.BOLD
					}
				}
				button("Cancel".translate(Language.TranslationTypes.AppointmentPopup)) {
					isCancelButton = true
					action {
						close()
					}
				}
				button(saveTitle) {
					isDefaultButton = true
					action {
						val check = checkAppointment()
						if(check == null) {
							if(appointment == null)
								appointment = createAppointment()
							updateAppointment()
							onSave.invoke(appointment!!)
							close()
						} else {
							error.value = check
						}
					}
				}
			}
		}
	}
	
	class ItemsScope(
		val title: String, val saveTitle: String, val appointment: Appointment?,
		val start: LocalDateTime, val end: LocalDateTime, val save: (Appointment) -> Unit
	): Scope()
	
	companion object {
		@Suppress("LongParameterList")
		fun open(title: String, saveTitle: String, block: Boolean, appointment: Appointment?, start: LocalDateTime, end: LocalDateTime, save: (Appointment) -> Unit): Stage? {
			val scope = ItemsScope(title, saveTitle, appointment, start, end, save)
			return find<AppointmentPopup>(scope).openModal(modality = if(block) Modality.APPLICATION_MODAL else Modality.NONE, escapeClosesWindow = false)
		}
	}
}
