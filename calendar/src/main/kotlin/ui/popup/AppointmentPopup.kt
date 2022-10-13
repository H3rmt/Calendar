package ui.popup

import calendar.Appointment
import calendar.Timing
import calendar.Type
import calendar.Types
import ui.picker.dateTimePicker.dateTimePicker
import ui.styles.GlobalStyles
import ui.typeCombobox
import javafx.beans.property.*
import javafx.scene.layout.*
import javafx.scene.paint.*
import javafx.scene.text.*
import javafx.stage.*
import logic.Language
import logic.ObservableValueListeners.listen
import logic.log
import logic.translate
import tornadofx.*
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * AppointmentPopup class deriving from Fragment
 *
 * opened with AppointmentPopup.open(...)
 */
class AppointmentPopup: Fragment() {
	// scope to get parameter from
	override val scope = when(super.scope) {
		is NewScope -> {
			super.scope as NewScope
		}

		is EditScope -> {
			super.scope as EditScope
		}

		else -> super.scope
	}

	// set if AppointmentPopup is a new Appointment Popup
	private val new: NewScope? = (scope as? NewScope)

	// set if AppointmentPopup is an edit Appointment Popup
	private val edit: EditScope? = (scope as? EditScope)


	// get appointment to edit (if null create new Popup, else edit Popup)
	private var appointment: Appointment? = edit?.appointment

	// do not bind directly, instead copy values into new Observables, to only update original on updateAppointment()
	private var start: Property<LocalDateTime> = appointment?.start?.cloneProp()
		?: new?.start?.toProperty()
		?: Timing.getNow().toProperty()  // this is not possible, as eater appointment or scope has a start
	private var end: Property<LocalDateTime> = appointment?.end?.cloneProp()
		?: new?.end?.toProperty()
		?: Timing.getNow().plusHours(1).toProperty()  // this is not possible, as eater appointment or scope has an end
	private var appointmentTitle: Property<String> = appointment?.title?.cloneProp()
		?: "".toProperty()
	private var description: Property<String> = appointment?.description?.cloneProp()
		?: "".toProperty()
	private var type: Property<Type> = appointment?.type?.cloneProp()
		?: Types.getRandom("Appointment").toProperty()
	private var wholeDay: Property<Boolean> = appointment?.allDay?.cloneProp()
		?: false.toProperty()

	// gets used if wholeDay = true
	private var day: Property<LocalDate> = start.value.toLocalDate().toProperty()

	private var error: Property<String> = "".toProperty()

	private lateinit var control: BorderPane

	private var windowTitle: String = (if(appointment == null) "new appointment" else "edit appointment").translate(Language.TranslationTypes.AppointmentPopup)
	private var saveTitle: String = (if(appointment == null) "create" else "edit").translate(Language.TranslationTypes.AppointmentPopup)


	/**
	 * updates the display to show either datepicker or 2 dateTimePicker
	 * depending on wholeDay
	 */
	private fun updateDisplay(toggle: Boolean) {
		control.left = if(toggle) {
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

	/** updates Appointment */
	private fun updateAppointment() {
		if(wholeDay.value) {
			appointment?.let { app ->
				app.start.set(day.value.atStartOfDay())
				app.end.set(day.value.plusDays(1).atStartOfDay())
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

	/** creates Appointment */
	private fun createAppointment() {
		if(wholeDay.value) {
			appointment = Appointment.new(
				_start = day.value.atStartOfDay(),
				_end = day.value.plusDays(1).atStartOfDay(),
				_title = appointmentTitle.value,
				_description = description.value,
				_type = type.value,
				_allDay = true
			)
		} else {
			appointment = Appointment.new(
				_start = start.value,
				_end = end.value,
				_title = appointmentTitle.value,
				_description = description.value,
				_type = type.value,
				_allDay = false
			)
		}
	}

	/** check inputs before saving */
	private fun checkAppointment(): String? {
		return if(appointmentTitle.value.isEmpty()) {
			"missing title".translate(Language.TranslationTypes.AppointmentPopup)
		} else if(start.value.isAfter(end.value)) {
			"start is after end".translate(Language.TranslationTypes.AppointmentPopup)
		} else {
			null
		}
	}

	override fun onBeforeShow() {
		modalStage?.height = 320.0
		modalStage?.width = 500.0
		// TODO minHeight, minWidth
	}

	override val root = form {
		log("creating appointment popup")
		addClass(GlobalStyles.background_)
		fieldset(windowTitle) {
			addClass(GlobalStyles.maxHeight_)
			field("type".translate(Language.TranslationTypes.AppointmentPopup)) {
				typeCombobox(type)
				checkbox("whole day".translate(Language.TranslationTypes.AppointmentPopup), property = wholeDay) {
					style {
						padding = box(0.px, 0.px, 0.px, 40.px)
					}
				}
			}
			// shows datePicker or dateTimePicker
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
				// save or create Appointment
				button(saveTitle) {
					isDefaultButton = true
					action {
						val check = checkAppointment()
						if(check == null) {
							if(appointment == null)
								createAppointment()
							else
								updateAppointment()
							close()
						} else {
							error.value = check
						}
					}
				}
			}
		}
		log("created appointment popup")
	}

	init {
		wholeDay.listen(::updateDisplay, runOnce = true)
	}

	companion object {
		class EditScope(
			val appointment: Appointment
		): Scope()

		class NewScope(
			val start: LocalDateTime,
			val end: LocalDateTime,
		): Scope()

		/**
		 * opens a new AppointmentPopup
		 *
		 * @param start start time for new created appointment
		 * @param end end time for new created appointment
		 * @param block blocks doesn't allow the main window to get focused again
		 */
		fun openNew(start: LocalDateTime, end: LocalDateTime, block: Boolean = false): Stage? {
			val scope = NewScope(start, end)
			return find<AppointmentPopup>(scope).openModal(
				modality = if(block) Modality.APPLICATION_MODAL else Modality.NONE,
				escapeClosesWindow = false
			)
		}

		/**
		 * opens an edit AppointmentPopup
		 *
		 * @param appointment appointment to edit
		 * @param block blocks doesn't allow the main window to get focused again
		 */
		fun openEdit(appointment: Appointment, block: Boolean = false): Stage? {
			val scope = EditScope(appointment)
			return find<AppointmentPopup>(scope).openModal(
				modality = if(block) Modality.APPLICATION_MODAL else Modality.NONE,
				escapeClosesWindow = false
			)
		}
	}
}
