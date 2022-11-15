package ui.popup

import calendar.Appointment
import calendar.Appointments
import calendar.Reminder
import calendar.Timing
import ui.TranslatingSimpleStringProperty
import ui.picker.appointmentPicker.AppointmentPicker
import ui.picker.dateTimePicker.DateTimePicker
import ui.styles.GlobalStyles
import ui.toggleSwitch
import javafx.beans.property.*
import javafx.scene.layout.*
import javafx.scene.paint.*
import javafx.scene.text.*
import javafx.stage.*
import logic.Language
import logic.ObservableValueListeners.listen
import logic.log
import logic.translate
import nullIfValueNull
import tornadofx.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * AppointmentPopup class deriving from Fragment
 *
 * opened with AppointmentPopup.open(...)
 */
class ReminderPopup: Fragment() {
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

	// set if AppointmentPopup is a edit Appointment Popup
	private val edit: EditScope? = (scope as? EditScope)

	// get reminder to edit (if null create new Popup, else edit Popup)
	private var reminder: Reminder? = edit?.reminder

	private val appointment: Property<Appointment?> =
		edit?.reminder?.appointment?.cloneProp() // get appointment from reminder
			?: new?.appointment?.toProperty<Appointment>()   // get appointment from scope
			?: SimpleObjectProperty<Appointment?>(null)
	private val deadline: Property<LocalDateTime> =
		edit?.reminder?.deadline?.cloneProp()?.nullIfValueNull() // get deadline from reminder
			?: new?.deadline?.toProperty()      // get deadline from scope
			?: new?.appointment?.start?.cloneProp()
			?: SimpleObjectProperty(Timing.getNow())
	private val reminderTitle: Property<String> = edit?.reminder?.title?.cloneProp()
		?: SimpleObjectProperty("")
	private val description: Property<String> = edit?.reminder?.description?.cloneProp()
		?: SimpleObjectProperty("")

	// sets to no deadline if no deadline and appointment is set in reminder
	private var noDeadline: Property<Boolean> = (edit?.reminder != null && edit.reminder.deadline.value == null && edit.reminder.appointment.value == null).toProperty()

	// toggle to switch between deadline and appointment
	private val deadlineOrAppointment: Property<Boolean> = (appointment.value == null).toProperty()

	// name of toggle switch for deadline and appointment
	private val finishName: TranslatingSimpleStringProperty = TranslatingSimpleStringProperty("", Language.TranslationTypes.ReminderPopup)

	private val error: Property<String> = "".toProperty()

	lateinit private var control: BorderPane
	lateinit private var deadlineFiled: Field

	private var windowTitle: String = (if(reminder == null) "new reminder" else "edit reminder").translate(Language.TranslationTypes.ReminderPopup)
	private var saveTitle: String = (if(reminder == null) "create" else "edit").translate(Language.TranslationTypes.ReminderPopup)


	/** updates Reminder */
	private fun updateReminder() {
		reminder?.let { rem ->
			if(noDeadline.value) {
				rem.deadline.set(null)
				rem.appointment.set(null)
			} else {
				// only set currently visible value
				if(deadlineOrAppointment.value) {
					rem.deadline.set(deadline.value)
					rem.appointment.set(null)
				} else {
					rem.deadline.set(null)
					rem.appointment.set(appointment)
				}
			}
			rem.title.set(reminderTitle)
			rem.description.set(description)
		}
	}

	/** creates Reminder */
	private fun createReminder(): Reminder = if(noDeadline.value) {
		Reminder.new(
			_deadline = null,
			_appointment = null,
			_title = reminderTitle.value,
			_description = description.value,
		)
	} else {
		Reminder.new(
			_deadline = deadline.value,
			_appointment = appointment.value,
			_title = reminderTitle.value,
			_description = description.value,
		)
	}

	/** check inputs bevore saving */
	private fun checkReminder(): String? {
		if(reminderTitle.value.isEmpty()) {
			return "missing title".translate(Language.TranslationTypes.ReminderPopup)
		}
		return null
	}

	override fun onBeforeShow() {
		modalStage?.height = 320.0
		modalStage?.width = 440.0
		modalStage?.minWidth = 430.0
		modalStage?.minHeight = 280.0
	}

	override val root = form {
		log("creating reminder popup")
		addClass(GlobalStyles.background_)
		fieldset(windowTitle) {
			addClass(GlobalStyles.maxHeight_)
			field("no deadline".translate(Language.TranslationTypes.ReminderPopup)) {
				toggleSwitch(selected = noDeadline)
			}
			deadlineFiled = field("finish".translate(Language.TranslationTypes.ReminderPopup)) {
				// shows DateTimePicker or AppointmentPicker
				control = borderpane {
					right = toggleSwitch(text = finishName, selected = deadlineOrAppointment) {
					}
				}
			}
			field("title".translate(Language.TranslationTypes.ReminderPopup)) {
				textfield(reminderTitle)
			}
			field("description".translate(Language.TranslationTypes.ReminderPopup)) {
				addClass(GlobalStyles.maxHeight_)
				style(append = true) {
					minHeight = 60.px
					padding = box(0.px, 0.px, 20.px, 0.px)
				}
				textarea(description) {
					addClass(GlobalStyles.maxHeight_)
				}
			}
			field("notify".translate(Language.TranslationTypes.ReminderPopup)) {
				text("missing %s".translate(Language.TranslationTypes.Global, "Notifications"))
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
				button("cancel".translate(Language.TranslationTypes.ReminderPopup)) {
					isCancelButton = true
					action {
						close()
					}
				}
				// save or create Reminder
				button(saveTitle) {
					isDefaultButton = true
					action {
						val check = checkReminder()
						if(check == null) {
							if(reminder == null)
								reminder = createReminder()
							updateReminder()
							close()
						} else {
							error.value = check
						}
					}
				}
			}
		}

		log("created reminder popup")
	}

	init {
		// switch between control for AppointmentPicker and DatePicker
		deadlineOrAppointment.listen(runOnce = true) { it ->
			if(it) {
				finishName.value = "Date"
				control.left = DateTimePicker(dateTime = deadline, formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM))
			} else {
				finishName.value = "Appointment"
				control.left = AppointmentPicker(appointments = Appointments, appointment = appointment)
			}
		}
		// disable deadline pickers of no Deadline selected
		noDeadline.listen(runOnce = true) { it ->
			deadlineFiled.isVisible = !it
			deadlineFiled.isManaged = !it
		}
	}

	companion object {
		class EditScope(
			val reminder: Reminder,
		): Scope()

		class NewScope(
			val deadline: LocalDateTime?,
			val appointment: Appointment?
		): Scope()


		/**
		 * opens a new ReminderPopup
		 *
		 * @param deadline deadline for new reminder
		 * @param appointment appointment as deadline for reminder
		 * @param block blocks doesn't allow the main window to get focused again
		 */
		fun openNew(deadline: LocalDateTime?, appointment: Appointment?, block: Boolean = false): Stage? {
			val scope = NewScope(deadline, appointment)
			return find<ReminderPopup>(scope).openModal(
				modality = if(block) Modality.APPLICATION_MODAL else Modality.NONE,
				escapeClosesWindow = false
			)
		}

		/**
		 * opens a edit ReminderPopup
		 *
		 * @param reminder reminder to edit
		 * @param block blocks doesn't allow the main window to get focused again
		 */
		fun openEdit(reminder: Reminder, block: Boolean = false): Stage? {
			val scope = EditScope(reminder)
			return find<ReminderPopup>(scope).openModal(
				modality = if(block) Modality.APPLICATION_MODAL else Modality.NONE,
				escapeClosesWindow = false
			)
		}
	}
}
