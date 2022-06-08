package frame.popup

import calendar.Appointment
import calendar.Appointments
import calendar.Reminder
import calendar.Timing
import frame.styles.GlobalStyles
import frame.toggleSwitch
import javafx.beans.property.*
import javafx.scene.layout.*
import javafx.scene.paint.*
import javafx.scene.text.*
import javafx.stage.*
import logic.Language
import logic.listen
import logic.translate
import nullIfValueNull
import picker.appointmentPicker.AppointmentPicker
import picker.dateTimePicker.DateTimePicker
import tornadofx.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


class ReminderPopup: Fragment() {
	override val scope = when(super.scope) {
		is NewScope -> {
			super.scope as NewScope
		}
		is EditScope -> {
			super.scope as EditScope
		}
		else -> super.scope
	}

	private val new: NewScope? = (scope as? NewScope)
	private val edit: EditScope? = (scope as? EditScope)

	private var reminder: Reminder? = edit?.reminder

	private val appointment: Property<Appointment?> = edit?.reminder?.appointment?.cloneProp()
		?: new?.appointment?.toProperty() as? Property<Appointment?>
		?: SimpleObjectProperty()
	private val deadline: Property<LocalDateTime> = edit?.reminder?.deadline?.cloneProp()?.nullIfValueNull()
		?: new?.time?.toProperty()
		?: new?.appointment?.start?.cloneProp()
		?: SimpleObjectProperty(Timing.getNow())
	private val reminderTitle: Property<String> = edit?.reminder?.title?.cloneProp()
		?: SimpleObjectProperty("")
	private val description: Property<String> = edit?.reminder?.description?.cloneProp()
		?: SimpleObjectProperty("")
	private var noDeadline: Property<Boolean> = (edit?.reminder != null && edit.reminder.deadline.value == null && edit.reminder.appointment.value == null).toProperty()

	private val windowTitle: String = edit?.run { "Edit Reminder".translate(Language.TranslationTypes.ReminderPopup) }
		?: "New Reminder".translate(Language.TranslationTypes.ReminderPopup)
	private val saveTitle: String = edit?.run { "Save".translate(Language.TranslationTypes.ReminderPopup) }
		?: "Create".translate(Language.TranslationTypes.ReminderPopup)

	private val deadlineOrAppointment: Property<Boolean> = (deadline.value != null).toProperty()
	private val finishName: Property<String> = "".toProperty()

	private lateinit var control: BorderPane
	private lateinit var deadlineFiled: Field
	private val error: Property<String> = "".toProperty()

	private fun updateReminder() {
		reminder?.let { rem ->
			if(noDeadline.value) {
				rem.deadline.set(null)
				rem.appointment.set(null)
			} else {
				rem.deadline.set(deadline.value)
				rem.appointment.set(appointment)
			}
			rem.title.set(reminderTitle)
			rem.description.set(description)
			rem.appointment.set(appointment)
		}
	}

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
		addClass(GlobalStyles.background_)
		fieldset(windowTitle) {
			addClass(GlobalStyles.maxHeight_)
			field("no Deadline".translate(Language.TranslationTypes.ReminderPopup)) {
				toggleSwitch(selected = noDeadline)
			}
			field("finish".translate(Language.TranslationTypes.ReminderPopup)) {
				borderpane {
					right = toggleSwitch(text = finishName, selected = deadlineOrAppointment) {
					}
				}.also { control = it }
			}.also { deadlineFiled = it }
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
				text("missing %s".translate(Language.TranslationTypes.ReminderPopup, "Notifications"))
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
	}

	init {
		// switch between control for AppointmentPicker and DatePicker
		deadlineOrAppointment.listen(runOnce = true) {
			if(it) {
				finishName.value = "Appointment"
				control.left = AppointmentPicker(appointments = Appointments, appointmentProperty = appointment)
			} else {
				finishName.value = "Date"
				control.left = DateTimePicker(dateTime = deadline, formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM))
			}
		}
		// disable deadline pickers of no Deadline selected
		noDeadline.listen(runOnce = true) {
			deadlineFiled.isVisible = !it
			deadlineFiled.isManaged = !it
		}
	}

	companion object {
		class EditScope(
			val reminder: Reminder,
		): Scope()

		class NewScope(
			val time: LocalDateTime?,
			val appointment: Appointment?
		): Scope()

		fun openNew(time: LocalDateTime?, appointment: Appointment?, block: Boolean = false): Stage? {
			val scope = NewScope(time, appointment)
			return find<ReminderPopup>(scope).openModal(
				modality = if(block) Modality.APPLICATION_MODAL else Modality.NONE,
				escapeClosesWindow = false
			)
		}

		fun openEdit(reminder: Reminder, block: Boolean = false): Stage? {
			val scope = EditScope(reminder)
			return find<ReminderPopup>(scope).openModal(
				modality = if(block) Modality.APPLICATION_MODAL else Modality.NONE,
				escapeClosesWindow = false
			)
		}
	}
}
