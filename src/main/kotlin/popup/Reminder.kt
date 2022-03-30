package popup

import calendar.Reminder
import calendar.Timing
import datetimepicker.dateTimePicker
import javafx.beans.property.*
import javafx.scene.layout.*
import javafx.scene.paint.*
import javafx.scene.text.*
import javafx.stage.*
import logic.getLangString
import tornadofx.*
import java.time.LocalDateTime


class NewReminderPopup: Fragment() {
	override val scope = super.scope as ItemsScope
	
	private var reminder: Reminder? = scope.reminder
	
	// do not bind directly, instead copy values into new Observables, to only save an updateAppointment()
	private var reminderTitle: Property<String> = (reminder?.title ?: "").toProperty()
	private var end: Property<LocalDateTime> = (reminder?.let { Timing.UTCEpochMinuteToLocalDateTime(it.time) } ?: scope.end).toProperty()
	private var reminderDescription: Property<String> = (reminder?.title ?: "").toProperty()
	
	private var onSave: (Reminder) -> Unit = scope.save
	
	private var error: Property<String> = "".toProperty()
	
	private var windowTitle: String = scope.title
	private var saveTitle: String = scope.saveTitle
	
	private fun updateReminder() {
		reminder?.let { rem ->
			rem.title = reminderTitle.value
		}
	}
	
	private fun createReminder(): Reminder = Reminder.new(
		12, reminderTitle.value, reminderDescription.value,
	)
	
	private fun checkAppointment(): String? {
		if(reminderTitle.value.isEmpty()) {
			return getLangString("missing title")
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
			field(getLangString("Finish")) {
				dateTimePicker(dateTime = end)
			}
			field(getLangString("title")) {
				textfield(reminderTitle)
			}
			field(getLangString("description")) {
				style(append = true) {
					prefHeight = Int.MAX_VALUE.px
					minHeight = 60.px
					padding = box(0.px, 0.px, 20.px, 0.px)
				}
				textarea(reminderDescription) {
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
	
	class ItemsScope(
		val title: String, val saveTitle: String, val reminder: Reminder?, val end: LocalDateTime, val save: (Reminder) -> Unit
	): Scope()
	
	companion object {
		fun open(title: String, saveTitle: String, block: Boolean, reminder: Reminder?, end: LocalDateTime, save: (Reminder) -> Unit): Stage? {
			val scope = ItemsScope(title, saveTitle, reminder, end, save)
			return find<NewReminderPopup>(scope).openModal(modality = if(block) Modality.APPLICATION_MODAL else Modality.NONE, escapeClosesWindow = false)
		}
	}
}
