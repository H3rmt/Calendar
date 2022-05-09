package frame.popup

import calendar.Appointment
import calendar.Reminder
import calendar.Timing
import calendar.Timing.toUTCEpochMinute
import calendar.getAppointments
import frame.styles.GlobalStyles
import frame.toggleSwitch
import javafx.beans.property.*
import javafx.geometry.*
import javafx.scene.layout.*
import javafx.scene.paint.*
import javafx.scene.text.*
import javafx.stage.*
import listen
import logic.Language
import logic.translate
import picker.appointmentPicker.appointmentPicker
import picker.dateTimePicker.dateTimePicker
import tornadofx.*
import java.time.LocalDateTime


class ReminderPopup: Fragment() {
	override val scope = super.scope as ItemsScope
	
	private var reminder: Reminder? = scope.reminder
	
	// do not bind directly, instead copy values into new Observables, to only save an updateAppointment()
	private var reminderTitle: Property<String> = (reminder?.title ?: "").toProperty()
	private var end: Property<LocalDateTime> = (reminder?.let { it.time?.let { it1 -> Timing.fromUTCEpochMinuteToLocalDateTime(it1) } } ?: scope.end).toProperty()
	private var reminderDescription: Property<String> = (reminder?.title ?: "").toProperty()
	
	private var onSave: (Reminder) -> Unit = scope.save
	
	private var error: Property<String> = "".toProperty()
	
	private var windowTitle: String = scope.title
	private var saveTitle: String = scope.saveTitle
	
	private var toggle: Property<Boolean> = scope.timeOrAppointment.toProperty()
	private var toggleName: Property<String> = "".toProperty()
	private var control: BorderPane? = null
	private var appointment: Property<Appointment?> = SimpleObjectProperty(null)
	
	init {
		appointment.listen {
			if(it != null)
				end.value = Timing.fromUTCEpochMinuteToLocalDateTime(it.start)
			else
				end.value = scope.end
		}
	}
	
	private fun updateDisplay(toggle: Boolean) {
		if(toggle) {
			toggleName.value = "Appointment"
			control?.left = appointmentPicker(getAppointments(), appointment = appointment)
		} else {
			toggleName.value = "Date"
			control?.left = dateTimePicker(dateTime = end)
		}
	}
	
	private fun updateReminder() {
		reminder?.let { rem ->
			rem.title = reminderTitle.value
		}
	}
	
	private fun createReminder(): Reminder = Reminder.new(
		end.value.toUTCEpochMinute(), reminderTitle.value, reminderDescription.value,
	)
	
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
			field("finish".translate(Language.TranslationTypes.ReminderPopup)) {
				control = borderpane {
					right = stackpane {
						alignment = Pos.CENTER_RIGHT
						style {
							paddingLeft = 5
						}
						label(toggleName) {
							style {
								paddingRight = 38
							}
						}
						toggleSwitch(selected = toggle) {
						}
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
				textarea(reminderDescription) {
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
							onSave.invoke(reminder!!)
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
		toggle.listen {
			updateDisplay(it)
		}
		updateDisplay(toggle.value)
	}
	
	class ItemsScope(
		val title: String, val saveTitle: String, val reminder: Reminder?, val end: LocalDateTime, val save: (Reminder) -> Unit, val timeOrAppointment: Boolean
	): Scope()
	
	companion object {
		@Suppress("LongParameterList")
		fun open(title: String, saveTitle: String, block: Boolean, reminder: Reminder?, end: LocalDateTime, save: (Reminder) -> Unit, timeOrAppointment: Boolean = true): Stage? {
			val scope = ItemsScope(title, saveTitle, reminder, end, save, timeOrAppointment)
			return find<ReminderPopup>(scope).openModal(modality = if(block) Modality.APPLICATION_MODAL else Modality.NONE, escapeClosesWindow = false)
		}
	}
}