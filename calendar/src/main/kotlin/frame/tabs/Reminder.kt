package frame.tabs

import calendar.Reminder
import calendar.Reminders
import frame.adjustWidth
import frame.styles.GlobalStyles
import frame.styles.ReminderStyles
import frame.styles.TabStyles
import javafx.beans.property.DoubleProperty
import javafx.geometry.Pos
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import logic.*
import tornadofx.*


fun createReminderTab(pane: TabPane): Tab {
	log("creating reminder tab")
	return pane.tab("reminders".translate(Language.TranslationTypes.Reminder)) {
		isClosable = false
		addClass(TabStyles.tab_)

		vbox {
			hbox(spacing = 20.0, alignment = Pos.CENTER) {
				addClass(TabStyles.topbar_)
				label("reminder".translate(Language.TranslationTypes.Reminder)) {
					addClass(TabStyles.title_)
					minWidth = 200.0
					alignment = Pos.CENTER
				}
			}

			vbox(spacing = 1.0, alignment = Pos.TOP_CENTER) {
				addClass(GlobalStyles.disableFocusDraw_)
				addClass(GlobalStyles.table_)

				lateinit var scrollbarWidth: DoubleProperty

				// Top bar
				hbox(spacing = 5.0, alignment = Pos.CENTER) {
					addClass(GlobalStyles.tableHeader_)

					scrollbarWidth = paddingRightProperty
					for(header in arrayListOf("Title", "Deadline", "Description")) {
						label(header.translate(Language.TranslationTypes.Reminder)) {
							addClass(GlobalStyles.tableItem_)
							addClass(GlobalStyles.tableHeaderItem_)
							addClass(ReminderStyles.tableHeaderItem_)
						}
					}
				}

				scrollpane(fitToWidth = true, fitToHeight = true) {
					addClass(GlobalStyles.disableFocusDraw_)
					addClass(GlobalStyles.maxHeight_)
					addClass(GlobalStyles.background_)
					isPannable = true

					// update top bar fake scrollbar padding  (wait for width update,so that scrollbars were created already; and then update if scrollbar width changes[appears/disappears])
					adjustWidth(scrollbarWidth)

					// gets stretched across whole scrollpane
					vbox(spacing = 2.0, alignment = Pos.TOP_CENTER) {
						addClass(GlobalStyles.background_)

						var reminderRows = mutableMapOf<Reminder, HBox>()

						Reminders.listenUpdates { change ->
							while(change.next()) {
								if(change.wasAdded()) {
									for(reminder in change.addedSubList) {
										reminderRows[reminder] = reminderRow(this, reminder)
									}
								}
								if(change.wasRemoved()) {
									for(reminder in change.removed) {
										// custom filtering, as notes are not the exact same (this will always return only one element)
										this.children.removeAll(reminderRows.filter { it.key == reminder }.values)
										reminderRows = reminderRows.filter { it.key != reminder }.toMutableMap()
									}
								}
							}
						}
						for(reminder in Reminders) {
							reminderRows[reminder] = reminderRow(this, reminder)
						}
					}
				}
			}
		}
		log("created reminder tab")
	}
}

fun reminderRow(tabs: VBox, reminder: Reminder): HBox {
	return tabs.hbox(spacing = 5.0, alignment = Pos.CENTER) {
		addClass(ReminderStyles.reminderRow_)
		label(reminder.title) {
			addClass(GlobalStyles.tableItem_)
			addClass(ReminderStyles.tableItemLeft_)
		}
		label(reminder.time) {
			addClass(GlobalStyles.tableItem_)
			addClass(ReminderStyles.tableItemMiddle_)
		}
		label(reminder.description) {
			addClass(GlobalStyles.tableItem_)
			addClass(ReminderStyles.tableItemRight_)
		}
	}
}
