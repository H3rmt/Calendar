package frame

import javafx.geometry.*
import javafx.scene.control.*
import logic.LogType
import logic.getLangString
import logic.log
import tornadofx.*


fun createReminderTab(pane: TabPane, updateCallback: () -> Unit): Tab {
	log("creating week tab", LogType.IMPORTANT)
	return pane.tab(getLangString("reminder")) {
		isClosable = true
		
		stackpane {
			style(append = true) {
				padding = box(6.px)
			}
			
			// main tab
			vbox {
				addClass(Styles.Tabs.mainTab)
				style {
					padding = box(0.px, 0.px, 2.px, 0.px)
				}
				
				hbox(spacing = 40.0, alignment = Pos.CENTER_LEFT) {
					addClass(Styles.Tabs.topbar)
					style {
						padding = box(0.px, 15.px, 0.px, 15.px)
					}
					button {
						text = "Test"
						
						addClass(Styles.Tabs.titleButtons)
					}
				}
				
				separate()
			}
			
			// used to shadow the overflow from tab
			pane {
				isMouseTransparent = true
				addClass(Styles.Tabs.shadowBorder)
			}
		}
		
	}
}

/*
class NewAppointmentPopup: Fragment() {
	override val scope = super.scope as ItemsScope
	
	private var reminder: Reminder? = scope.reminder
	
	// do not bind directly, instead copy values into new Observables, to only save an updateAppointment()
	private var reminderTitle: Property<String> = (reminder?.title ?: "").toProperty()
	
	private var onSave: (Appointment) -> Unit = scope.save
	
	private var error: Property<String> = "".toProperty()
	
	private var windowTitle: String = scope.title
	private var saveTitle: String = scope.saveTitle
	
	private fun updateAppointment() {
		reminder?.let { rem ->
			app.title = reminderTitle.value
		}
	}
	
	private fun createAppointment(): Reminder = Reminder.new(
		reminderTitle
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
			field(getLangString("title")) {
				textfield(reminderTitle)
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
								reminder = createAppointment()
							updateAppointment()
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
		val title: String, val saveTitle: String, val reminder: Reminder?, val save: (Reminder) -> Unit
	): Scope()
	
	companion object {
		fun open(title: String, saveTitle: String, block: Boolean, reminder: Reminder?, save: (Reminder) -> Unit): Stage? {
			val scope = ItemsScope(title, saveTitle, reminder, save)
			return find<NewAppointmentPopup>(scope).openModal(modality = if(block) Modality.APPLICATION_MODAL else Modality.NONE, escapeClosesWindow = false)
		}
	}
	
}*/
