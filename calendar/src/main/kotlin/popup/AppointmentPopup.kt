package popup

import calendar.Appointment
import calendar.Timing
import calendar.Timing.toUTCEpochMinute
import calendar.getTypes
import javafx.beans.property.*
import javafx.scene.layout.*
import javafx.scene.paint.*
import javafx.scene.text.*
import javafx.stage.*
import listen
import logic.getLangString
import picker.dateTimePicker.dateTimePicker
import tornadofx.*
import java.time.LocalDate
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
	private var wholeDay: Property<Boolean> = (appointment?.allDay ?: false).toProperty()
	private var control: BorderPane? = null
	private var day: Property<LocalDate> = (appointment?.start?.let { Timing.fromUTCEpochMinuteToLocalDateTime(it).toLocalDate() } ?: scope.start.toLocalDate()).toProperty()
	
	private var windowTitle: String = scope.title
	private var saveTitle: String = scope.saveTitle
	
	private fun updateDisplay(toggle: Boolean) {
		control?.left = if(toggle) {
			field(getLangString("start to end")) {
				datepicker(property = day)
			}
		} else {
			field(getLangString("start to end")) {
				dateTimePicker(dateTime = start)
				dateTimePicker(dateTime = end)
			}
		}
	}
	
	private fun updateAppointment() {
		if(wholeDay.value) {
			appointment?.let { app ->
				app.title = appointmentTitle.value
				app.description = description.value
				app.start = day.value.toUTCEpochMinute()
				app.duration = 1439
				app.type = getTypes().find { it.name == type.value }!!
				app.allDay = true
			}
		} else {
			appointment?.let { app ->
				app.title = appointmentTitle.value
				app.description = description.value
				app.start = start.value.toUTCEpochMinute()
				app.duration = end.value.toUTCEpochMinute() - start.value.toUTCEpochMinute()
				app.type = getTypes().find { it.name == type.value }!!
				app.allDay = false
			}
		}
	}
	
	private fun createAppointment(): Appointment = if(wholeDay.value) {
		Appointment.new( // duration irrelevant
			_start = day.value.atStartOfDay().toUTCEpochMinute(),
			_duration = 1439, _title = appointmentTitle.value,
			_description = description.value, _type = getTypes().find { it.name == type.value }!!,
			_addDay = true
		)
	} else {
		Appointment.new(
			_start = start.value.toUTCEpochMinute(),
			_duration = end.value.toUTCEpochMinute() - start.value.toUTCEpochMinute(),
			_title = appointmentTitle.value,
			_description = description.value, _type = getTypes().find { it.name == type.value }!!
		)
	}
	
	@Suppress("ReturnCount")
	private fun checkAppointment(): String? {
		if(type.value == "") {
			return getLangString("missing type")
		} else if(appointmentTitle.value.isEmpty()) {
			return getLangString("missing title")
		} else if(end.value.toUTCEpochMinute() < start.value.toUTCEpochMinute()) {
			return getLangString("start must be before end")
		}
		return null
	}
	
	override fun onBeforeShow() {
		modalStage?.height = 320.0
		modalStage?.width = 400.0
	}
	
	override val root = form {
		style {
			backgroundColor += Color.WHITE
		}
		fieldset(getLangString(windowTitle)) {
			style {
				prefHeight = Int.MAX_VALUE.px
			}
			field("Type") {
				combobox(values = getTypes().map { it.name }, property = type)
				checkbox(getLangString("Whole day"), property = wholeDay)
			}
			control = borderpane()
			field(getLangString("title")) {
				textfield(appointmentTitle)
			}
			field(getLangString("description")) {
				style(append = true) {
					prefHeight = Int.MAX_VALUE.px
					minHeight = 60.px
					padding = box(0.px, 0.px, 20.px, 0.px)
				}
				textarea(description) {
					style(append = true) {
						prefHeight = Int.MAX_VALUE.px
					}
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
				button(getLangString("Cancel")) {
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
