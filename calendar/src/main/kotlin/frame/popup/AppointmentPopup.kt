package frame.popup

import calendar.Appointment
import calendar.Timing.toUTCEpochMinute
import calendar.Type
import calendar.getTypes
import frame.styles.GlobalStyles
import javafx.beans.property.*
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.paint.*
import javafx.scene.text.*
import javafx.stage.*
import listen
import logic.Language
import logic.translate
import picker.dateTimePicker.dateTimePicker
import tornadofx.*
import java.time.LocalDate
import java.time.LocalDateTime



class AppointmentPopup: Fragment() {
	override val scope = super.scope as ItemsScope
	
	private var appointment: Appointment? = scope.appointment
	
	// do not bind directly, instead copy values into new Observables, to only save an updateAppointment()
	private var start: Property<LocalDateTime> = appointment?.start?.clone() ?: scope.start.toProperty()
	private var end: Property<LocalDateTime> = appointment?.end?.clone() ?: scope.end.toProperty()
	private var appointmentTitle: Property<String> = appointment?.title?.clone() ?: "".toProperty()
	private var description: Property<String> = appointment?.description?.clone() ?: "".toProperty()
	private var type: Property<Type> = appointment?.type?.clone() ?: getTypes()[0].toProperty()
	private var wholeDay: Property<Boolean> = appointment?.allDay?.clone() ?: false.toProperty()
	
	private var onSave: (Appointment) -> Unit = scope.save
	
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
			_start = day.value.atStartOfDay(),
			_end = day.value.plusDays(1).atStartOfDay().minusMinutes(1),
			_title = appointmentTitle.value,
			_description = description.value,
			_type = type.value,
			_allDay = true
		)
	} else {
		Appointment.new(
			_start = start.value,
			_end = end.value,
			_title = appointmentTitle.value,
			_description = description.value,
			_type = type.value,
			_allDay = false
		)
	}
	
	@Suppress("ReturnCount")
	private fun checkAppointment(): String? {
		if(appointmentTitle.value.isEmpty()) {
			return "missing title".translate(Language.TranslationTypes.AppointmentPopup)
		} else if(end.value.toUTCEpochMinute() < start.value.toUTCEpochMinute()) {
			return "start must be before end".translate(Language.TranslationTypes.AppointmentPopup)
		}
		return null
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
				combobox(values = getTypes(), property = type) {
					setCellFactory { p ->
						return@setCellFactory object: ListCell<Type>() {
							override fun updateItem(item: Type, empty: Boolean) {
								super.updateItem(item, empty)
								text = item.name.value
							}
						}
					}
				}
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
	
	init {
		wholeDay.listen {
			updateDisplay(it)
		}
		updateDisplay(wholeDay.value)
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
