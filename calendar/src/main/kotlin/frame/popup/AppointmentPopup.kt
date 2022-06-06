package frame.popup

import calendar.Appointment
import calendar.Timing.toUTCEpochMinute
import calendar.Type
import calendar.Types
import frame.styles.GlobalStyles
import frame.typeCombobox
import javafx.beans.property.Property
import javafx.scene.layout.BorderPane
import javafx.scene.layout.BorderStrokeStyle
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import javafx.stage.Modality
import javafx.stage.Stage
import logic.Language
import logic.listen
import logic.translate
import picker.dateTimePicker.dateTimePicker
import tornadofx.*
import java.time.LocalDate
import java.time.LocalDateTime


class AppointmentPopup: Fragment() {
	override val scope = super.scope as ItemsScope
	
	private var appointment: Appointment? = scope.appointment
	
	// do not bind directly, instead copy values into new Observables, to only save an updateAppointment()
	private var start: Property<LocalDateTime> = appointment?.start?.cloneProp() ?: scope.start.toProperty()
	private var end: Property<LocalDateTime> = appointment?.end?.cloneProp() ?: scope.end.toProperty()
	private var appointmentTitle: Property<String> = appointment?.title?.cloneProp() ?: "".toProperty()
	private var description: Property<String> = appointment?.description?.cloneProp() ?: "".toProperty()
	private var type: Property<Type> = appointment?.type?.cloneProp() ?: Types.getRandom("Appointment").toProperty()
	private var wholeDay: Property<Boolean> = appointment?.allDay?.cloneProp() ?: false.toProperty()
	
	private var error: Property<String> = "".toProperty()
	private var control: BorderPane? = null
	private var day: Property<LocalDate> = (appointment?.start?.value ?: scope.start).toLocalDate().toProperty()
	
	private var windowTitle: String = scope.title
	private var saveTitle: String = scope.saveTitle
	
	private fun updateDisplay(toggle: Boolean) {
		control?.left = if(toggle) {
			field("day".translate(Language.TranslationTypes.AppointmentPopup)) {
				datepicker(property = day)
			}
		} else {
			field("start to end".translate(Language.TranslationTypes.AppointmentPopup)) {
				dateTimePicker(dateTime = start)
				dateTimePicker(dateTime = end)
			}
		}
	}
	
	private fun updateAppointment() {
		if(wholeDay.value) {
			appointment?.let { app ->
				app.start.set(day.value.atStartOfDay())
				app.end.set(day.value.plusDays(1).atStartOfDay().minusMinutes(1))
				app.title.set(appointmentTitle)
				app.description.set(description)
				app.type.set(type)
				app.allDay.set(true)
			}
		} else {
			appointment?.let { app ->
				app.start.set(start)
				app.end.set(end)
				app.title.set(appointmentTitle)
				app.description.set(description)
				app.type.set(type)
				app.allDay.set(true)
			}
		}
	}
	
	private fun createAppointment(): Appointment = if(wholeDay.value) {
		Appointment.new(
			start = day.value.atStartOfDay(),
			end = day.value.plusDays(1).atStartOfDay().minusMinutes(1),
			title = appointmentTitle.value,
			description = description.value,
			type = type.value,
			allDay = true
		)
	} else {
		Appointment.new(
			start = start.value,
			end = end.value,
			title = appointmentTitle.value,
			description = description.value,
			type = type.value,
			allDay = false
		)
	}
	
	private fun checkAppointment(): String? {
		return if(appointmentTitle.value.isEmpty()) {
			"missing title".translate(Language.TranslationTypes.AppointmentPopup)
		} else if(end.value.toUTCEpochMinute() < start.value.toUTCEpochMinute()) {
			"start must be before end".translate(Language.TranslationTypes.AppointmentPopup)
		} else {
			null
		}
	}
	
	override fun onBeforeShow() {
		modalStage?.height = 320.0
		modalStage?.width = 500.0
	}
	
	override val root = form {
		addClass(GlobalStyles.background_)
		fieldset(windowTitle) {
			addClass(GlobalStyles.maxHeight_)
			field("Type") {
				typeCombobox(type)
				checkbox("whole day".translate(Language.TranslationTypes.AppointmentPopup), property = wholeDay) {
					style {
						padding = box(0.px, 0.px, 0.px, 40.px)
					}
				}
			}
			control = borderpane()
			field("title".translate(Language.TranslationTypes.AppointmentPopup)) {
				textfield(appointmentTitle)
			}
			field("description".translate(Language.TranslationTypes.AppointmentPopup)) {
				addClass(GlobalStyles.maxHeight_)
				style(append = true) {
					minHeight = 60.px
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
				button("cancel".translate(Language.TranslationTypes.AppointmentPopup)) {
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
							close()
						} else {
							error.value = check
						}
					}
				}
			}
		}
	}
	
	init {
		wholeDay.listen(::updateDisplay, runOnce = true)
	}
	
	class ItemsScope(
		val title: String,
		val saveTitle: String,
		val appointment: Appointment?,
		val start: LocalDateTime,
		val end: LocalDateTime
	): Scope()
	
	companion object {
		@Suppress("LongParameterList")
		fun open(
			title: String,
			saveTitle: String,
			block: Boolean,
			appointment: Appointment?,
			start: LocalDateTime,
			end: LocalDateTime
		): Stage? {
			val scope = ItemsScope(title, saveTitle, appointment, start, end)
			return find<AppointmentPopup>(scope).openModal(
				modality = if(block) Modality.APPLICATION_MODAL else Modality.NONE, escapeClosesWindow = false
			)
		}
	}
}
